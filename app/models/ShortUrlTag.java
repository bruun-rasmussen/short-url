package models;

import javax.persistence.*;

import play.db.ebean.*;
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


      public static Finder<String,ShortUrlTag> find =
              new Finder(String.class, ShortUrlTag.class);

    @Override
    public String toString() {
        return "["+tag+" -> "+target+"]";
    }

}
