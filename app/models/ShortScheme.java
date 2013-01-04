package models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.*;
import org.apache.commons.lang.StringUtils;

import play.db.ebean.*;
import play.data.validation.*;

@Entity
public class ShortScheme extends Model {
    private Pattern pattern;

    public String expandTarget(String target) {
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
        Matcher m = pattern.matcher(target);
        if (!m.matches())
            throw new IllegalArgumentException("\"" + target + "\" does not match /"+pattern.pattern()+"/");
        return m.replaceFirst(replacement);
    }

    @Id
    @Constraints.Min(10)
    public Long id;

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String targetPattern;

    @Constraints.Required
    public String replacement;

    public static Finder<Long, ShortScheme> find =
            new Finder<Long, ShortScheme>(Long.class, ShortScheme.class);
}
