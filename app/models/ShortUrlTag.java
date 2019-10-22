package models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
@Table(uniqueConstraints =
                @UniqueConstraint(columnNames={"scheme_id", "target"}))
public class ShortUrlTag extends Model {

	  @Id
	  @Constraints.Min(10)
	  public String tag;

	  @Constraints.Required
      @ManyToOne
	  public ShortScheme scheme;

	  @Constraints.Required
	  public String target;


    public static Finder<String, ShortUrlTag> find =
            new Finder(String.class, ShortUrlTag.class);

    public String getShortcutUrl() {
        return scheme.getShortcutUrl(tag);
/*
        try {
            return scheme.tagPrefix + URLEncoder.encode(tag, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
 */ }

    public String getTargetUrl() {
        return scheme.expandTarget(target);
    }

    @Override
    public String toString() {
        return "[" + tag + " -> " + target + "]";
    }

}
