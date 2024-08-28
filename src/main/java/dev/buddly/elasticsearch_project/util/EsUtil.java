package dev.buddly.elasticsearch_project.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import dev.buddly.elasticsearch_project.dto.SearchRequest;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class EsUtil {
    public static Query createMatchAllQuery() {
        return Query.of(q -> q.matchAll(new MatchAllQuery.Builder().build()));
    }

    public static Supplier<Query> buildQueryForFieldAndValue(String fieldName, String searchValue) {
        return () -> Query.of(q -> q.match(buildMatchQueryForFieldAndValue(fieldName,searchValue)));
    }

    private static MatchQuery buildMatchQueryForFieldAndValue(String fieldName, String searchValue) {
        return new MatchQuery.Builder()
                .field(fieldName)
                .query(searchValue)
                .build();
    }

    public static Supplier<Query> createBoolQuery(SearchRequest dto) {
        return () -> Query.of(q -> q.bool(boolQuery(dto.getFieldName().get(0), dto.getSearchValue().get(0),
                dto.getFieldName().get(1), dto.getSearchValue().get(1))));
    }


    private static BoolQuery boolQuery(String key1, String value1, String key2, String value2) {
        return new BoolQuery.Builder()
                .filter(termQuery(key1,value1))//birebir eşleşicek
                .must(matchQuery(key2,value2))//biraz andırsa yeter
                .build();
    }

    private static Query matchQuery(String field, String value) {
        return Query.of(q->q.match(new MatchQuery.Builder().field(field).query(value).build()));
    }

    private static Query termQuery(String field, String value) {
        return Query.of(q->q.term(new TermQuery.Builder().field(field).value(value).build()));
    }

    public static Query buildAutoSuggestQuery(String name) {
        return Query.of(q->q.match(new MatchQuery.Builder()
                .field("name")
                .query(name)
                .analyzer("custom_index")
                .build()
        ));
    }
}
