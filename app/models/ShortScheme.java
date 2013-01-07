package models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.*;
import org.apache.commons.lang.StringUtils;

import play.db.ebean.*;
import play.data.validation.*;

@Entity
public class ShortScheme extends Model {
    private transient Pattern pattern;

    private Pattern getPattern() {
        if (pattern == null) {
            String p = targetPattern;
            if (StringUtils.isEmpty(p))
                p = "(.*)";
            if (!p.startsWith("^"))
                p = "^" + p;
            if (!p.endsWith("$"))
                p = p + "$";
            pattern = Pattern.compile(p);
        }
        return pattern;
    }

    public String expandTarget(String target) {
        Matcher m = getPattern().matcher(target);
        if (!m.matches())
            throw new IllegalArgumentException("\"" + target + "\" does not match /"+pattern.pattern()+"/");
        return m.replaceFirst(replacement);
    }

    public boolean accepts(String target) {
        return getPattern().matcher(target).matches();
    }

    String getShortcutUrl(String tag) {
        return tagPrefix + tag;
    }

    @Id
    public Long id;

    @Constraints.Required
    @Constraints.Pattern("[a-zA-Z_][a-zA-Z0-9-_]*")
    public String name;

    public String description;

    @Constraints.Required
    public String targetPattern;

    @Constraints.Required
    public String replacement;

    public String tagPrefix;

    public String tagAlphabet;

    @Constraints.Required
    @Constraints.Min(1)
    @Constraints.Max(20)
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

    public static Finder<Long, ShortScheme> find =
            new Finder<Long, ShortScheme>(Long.class, ShortScheme.class);
}
