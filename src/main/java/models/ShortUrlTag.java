package models;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.net.URI;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;

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
        return "[" + tag + " -> " + target + "]";
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
            shortcut.tag = scheme.generateTag();
            shortcut.target = target;
            shortcut.persist();

            Log.info(shortcut + ": shortcut created");
        }

        return shortcut;
    }
}
