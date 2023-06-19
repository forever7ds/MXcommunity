package com.example.mxcommunity.service;

import com.example.mxcommunity.entity.model.ThemePost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SearchService {

//    @Autowired
//    private ThemePostRepository themePostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    public void saveThemePost(ThemePost post) {
        System.out.println(post.getId() + "Saved!");
    }

    public void deleteThemePost(long entityId) {
        System.out.println(entityId + "Deleted");
    }

    public Page<ThemePost> searchThemePostByKeywords(String keyword, int currentPage, int limit){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("modifiedTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(currentPage, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();


        return elasticsearchTemplate.queryForPage(searchQuery, ThemePost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                // 获取命中的数据
                SearchHits hits = searchResponse.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                // 处理命中的数据
                List<ThemePost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    ThemePost post = new ThemePost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Long.valueOf(id));

                    String userId = hit.getSourceAsMap().get("creatorId").toString();
                    post.setCreatorId(Long.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String type = hit.getSourceAsMap().get("type").toString();
                    post.setType(Integer.valueOf(type));

                    String modifiedTime = hit.getSourceAsMap().get("modifiedTime").toString();
                    post.setCreateTime(Long.valueOf(modifiedTime));

                    String createTime = hit.getSourceAsMap().get("modifiedTime").toString();
                    post.setCreateTime(Long.valueOf(createTime));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的内容
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }
        });

    }


}
