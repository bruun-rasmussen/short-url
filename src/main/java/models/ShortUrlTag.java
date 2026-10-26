package models;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Slf4j
@Entity
@Table(name = "short_url_tag", uniqueConstraints = @UniqueConstraint(columnNames = { "scheme_id", "target" }))
public class ShortUrlTag extends PanacheEntityBase {

    @Id
    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne
    @JoinColumn(name = "scheme_id", nullable = false)
    public ShortScheme scheme;

    @Column(name = "target", nullable = false)
    @NotNull
    public String target;

    public String getShortcutUrl() {
        return scheme.getShortcutUrl(tag);
    }

    public URI getTargetURI() {
        return scheme.expandTarget(target);
    }

    @Override
    public String toString() {
        return "[" + tag + " \u279d " + target + "]";
    }

    public static ShortUrlTag findByTag(String tag) {
        return find("tag", tag).firstResult();
    }

    @Transactional
    public static ShortUrlTag findOrCreate(ShortScheme scheme, String target) {
        // Check if this target has a shortened tag already:
        ShortUrlTag shortcut = find("scheme = ?1 and target = ?2", scheme, target).firstResult();

        if (shortcut == null) {
            if (!scheme.accepts(target))
                throw new IllegalArgumentException(target + ": invalid shortcut target");

            shortcut = new ShortUrlTag();
            shortcut.scheme = scheme;
            shortcut.tag = generateUniqueTag(scheme);
            shortcut.target = target;
            shortcut.persist();

            log.info("{}: created", shortcut);
        }

        return shortcut;
    }

    private static final int MAX_TAG_ATTEMPTS = 10;

    private static String generateUniqueTag(ShortScheme scheme) {
        for (int attempt = 1; attempt <= MAX_TAG_ATTEMPTS; attempt++) {
            String tag = scheme.generateTag();
            if (count("tag", tag) == 0)
                return tag;

            log.debug("'{}': tag clash, retrying (attempt {})", tag, attempt);
        }
        throw new IllegalStateException(scheme.name + ": no unique tag found after " + MAX_TAG_ATTEMPTS + " attempts");
    }
}
