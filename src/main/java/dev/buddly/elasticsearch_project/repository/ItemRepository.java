package dev.buddly.elasticsearch_project.repository;

import dev.buddly.elasticsearch_project.model.Item;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item,String> {
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\":\"?0\"}},{\"match\": {\"brand\":\"?1\"}}]}}")
    List<Item> searchByNameAndBrand(String name, String brand);

    @Query("{\"bool\":{\"must\":{\"match_phrase_prefix\": {\"name\":\"?0\"}}}}")
    List<Item> customAutoSuggest(String name);
}
