package models;

import java.net.URI;
import java.util.regex.Matcher;
import org.apache.commons.lang.StringUtils;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity(name = "ShortScheme")
@Table(name = "short_scheme")
public class ShortScheme extends PanacheEntityBase {
    private transient java.util.regex.Pattern pattern;

    private java.util.regex.Pattern getPattern() {
        if (pattern == null) {
            String p = targetPattern;
            if (StringUtils.isEmpty(p))
                p = "(.*)";
            if (!p.startsWith("^"))
                p = "^" + p;
            if (!p.endsWith("$"))
                p = p + "$";
            pattern = java.util.regex.Pattern.compile(p);
        }
        return pattern;
    }

    public URI expandTarget(String target) {
        Matcher m = getPattern().matcher(target);
        if (!m.matches())
            throw new IllegalArgumentException("\"" + target + "\" does not match /" + pattern.pattern() + "/");

        return URI.create(m.replaceFirst(replacement));
    }

    public boolean accepts(String target) {
        return getPattern().matcher(target).matches();
    }

    String getShortcutUrl(String tag) {
        return shortcutPrefix + tag;
    }

    @Id
    public Long id;

    @Column(name = "name", unique = true)
    @Pattern(regexp = "[a-zA-Z_][a-zA-Z0-9-_]*")
    public String name;

    @Column(name = "description")
    public String description;

    @Column(name = "target_pattern")
    public String targetPattern;

    @Column(name = "replacement")
    public String replacement;

    @Column(name = "shortcut_prefix")
    public String shortcutPrefix;

    @Column(name = "tag_alphabet")
    @NotBlank
    public String tagAlphabet;

    @Column(name = "tag_length")
    @Min(1)
    @Max(20)
    public Integer tagLength;

    public String generateTag()
    {
      char alphabetChars[] = tagAlphabet.toCharArray();
      char tagChars[] = new char[tagLength];
      for (int i = 0; i < tagLength; i++)
      {
        int randomPos = (int)(Math.random() * alphabetChars.length);
        tagChars[i] = alphabetChars[randomPos];
      }
      return new String(tagChars);
    }
}
