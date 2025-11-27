package dev.charles.SimpleBlogAPI.config;


import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

public class CustomPostgreSQLDialect extends PostgreSQLDialect{
    public static final String SEARCH_TEXT_FTS = "SEARCH_TEXT";
    public CustomPostgreSQLDialect() {
        super();
    }
    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        BasicType<Boolean> resolveType = functionContributions.getTypeConfiguration()
                .getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN);
        functionContributions.getFunctionRegistry()
                .registerPattern("search_text",
                        "?1 @@ websearch_to_tsquery(?2::regconfig, ?3)"
                        , resolveType);
    }
}