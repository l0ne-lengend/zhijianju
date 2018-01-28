package cn.edu.iip.nju.service;

import cn.edu.iip.nju.dao.InjureCaseDao;
import cn.edu.iip.nju.dao.NewsDataDao;
import cn.edu.iip.nju.model.InjureCase;
import cn.edu.iip.nju.model.NewsData;
import cn.edu.iip.nju.util.CityProvince;
import cn.edu.iip.nju.util.InjureLevelUtil;
import cn.edu.iip.nju.util.WordLizeUtil.WordFilter;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by xu on 2017/12/6.
 */
@Service
public class NewsDataService {
    @Autowired
    private NewsDataDao newsDataDao;
    @Autowired
    private InjureCaseDao injureCaseDao;
    @Autowired
    private StringRedisTemplate template;
    private final int defaultPageSize = 40;


    private int pageNum() {
        long count = newsDataDao.countAllByIsInjureNewsEquals(true);
        //System.out.println(count);
        return (int) (count / 40 + 1);
    }

    public void saveInjureCase() {
        int pageNum = pageNum();
        for (int i = 0; i < pageNum; i++) {
            List<NewsData> newsDatas = newsDataDao.findAllByIsInjureNewsEquals(true,
                    new PageRequest(i, defaultPageSize)).getContent();
            for (NewsData newsData : newsDatas) {
                processEachNews(newsData);
            }

        }
    }

    //每个news对应一个injureCase
    private void processEachNews(NewsData newsData) {
        String content = newsData.getTitle() + newsData.getContent();
        Set<String> product = searchKeyWord(content, "product");
        if (product != null && product.size() > 0) {
            InjureCase injureCase = new InjureCase();
            injureCase.setUrl(newsData.getUrl());
            injureCase.setInjureTime(newsData.getPostTime());
            injureCase.setProductName(joinToString(product));
            Set<String> cities = searchKeyWord(content, "所有城市");
            injureCase.setInjureArea(joinToString(cities));
            String city = "";
            if (cities != null && cities.size() > 0) {
                try {
                    city = CityProvince.chooseProvinceOfCompany(cities.iterator().next());
                } catch (Exception e) {
                    city = "";
                } finally {
                    injureCase.setProvince(city);
                }
            }
            Set<String> injureType = Sets.union(searchKeyWord(content, "allInjureType"),
                    searchKeyWord(content, "allFengxian"));
            injureCase.setInjureType(joinToString(injureType));
            String degree = InjureLevelUtil.checkInjureLevel(joinToString(injureType));
            injureCase.setInjureDegree(degree);
//            System.out.println(injureCase);
            try {
                injureCaseDao.save(injureCase);
            }catch (org.springframework.dao.DataIntegrityViolationException e){
                System.out.println("__________________________________________重复的数据_____________________________________________");
            }

        }
    }

    /**
     * @param s:需要匹配的字符串
     * @param redisSetName:redis中该set的名称
     * @return
     */
    private Set<String> searchKeyWord(String s, String redisSetName) {
        Set<String> serial1 = template.opsForSet().members(redisSetName);
        WordFilter wordFilter = new WordFilter();
        wordFilter.init(serial1);
        wordFilter.doFilter(s);
        return wordFilter.getKeyword();
    }

    /**
     * @param s:需要匹配的字符串
     * @param keywords:关键字集合
     * @return
     */
    private Set<String> searchKeyWord(String s, Set<String> keywords) {
        WordFilter wordFilter = new WordFilter();
        wordFilter.init(keywords);
        wordFilter.doFilter(s);
        return wordFilter.getKeyword();
    }


    private String joinToString(Collection<String> collection) {
        if (collection.size() > 20) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (String s : collection) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    @Cacheable(value = "newsdataCount")
    public long count(){
        return newsDataDao.count();
    }
}
