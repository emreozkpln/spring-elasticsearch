package dev.buddly.elasticsearch_project.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.buddly.elasticsearch_project.dto.SearchRequest;
import dev.buddly.elasticsearch_project.model.Item;
import dev.buddly.elasticsearch_project.repository.ItemRepository;
import dev.buddly.elasticsearch_project.util.EsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final JsonDataService jsonDataService;

    public Item createIndex(Item item) {
        return itemRepository.save(item);
    }

    public void addItemsFromJson() {
        log.info("Adding items from json");
        List<Item> itemList = jsonDataService.readItemsFromJson();
        itemRepository.saveAll(itemList);
    }

    public List<Item> getAllDataFromIndex(String indexName) {
        var query = EsUtil.createMatchAllQuery();
        log.info("Elasticsearch query {}",query.toString());
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(
                    q -> q.index(indexName).query(query), Item.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}",response);
        return extractItemsFromResponse(response);
    }

    public List<Item> extractItemsFromResponse(SearchResponse<Item> response) {
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List<Item> searchItemsByFieldAndValue(SearchRequest request) {
        Supplier<Query> query = EsUtil.buildQueryForFieldAndValue(
                request.getFieldName().get(0),
                request.getSearchValue().get(0)
        );
        log.info("Elasticsearch query {}",query.toString());
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(
                    q -> q.index("items_index").query(query.get()), Item.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}",response);
        return extractItemsFromResponse(response);
    }

    public List<Item> searchItemsByNameAndBrandWithQuery(String name, String brand) {
        return itemRepository.searchByNameAndBrand(name,brand);
    }

    public List<Item> boolQuery(SearchRequest request) {
        var query = EsUtil.createBoolQuery(request);
        log.info("Elasticsearch query {}",query.toString());
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(
                    q -> q.index("items_index").query(query.get()), Item.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}",response.toString());
        return extractItemsFromResponse(response);
    }

    public Set<String> findSuggestedItemNames(String name) {//set olmasının sebebi aynılarını bidaha görmememiz için
        Query query = EsUtil.buildAutoSuggestQuery(name);
        log.info("Elasticsearch query {}",query.toString());
        try {
            return elasticsearchClient.search(q->q.index("items_index").query(query), Item.class)
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .map(Item::getName)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> autoSuggestItemsByNameWithQuery(String name) {
        List<Item> itemList = itemRepository.customAutoSuggest(name);
        return itemList
                .stream()
                .map(Item::getName)
                .collect(Collectors.toList());
    }
}
