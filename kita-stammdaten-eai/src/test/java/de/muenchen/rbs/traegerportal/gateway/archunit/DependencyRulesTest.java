package de.muenchen.rbs.traegerportal.gateway.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.github.benmanes.caffeine.cache.Cache;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class DependencyRulesTest {

    @Test
    void cachesMustUseCaffeine() {
        final var classes = new ClassFileImporter().importPackages("de.muenchen.rbs.traegerportal");

        fields()
                .that().haveNameMatching("(?i).*cache")
                .should().haveRawType(Cache.class)
                .check(classes);
    }
}
