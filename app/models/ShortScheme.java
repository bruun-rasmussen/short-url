package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class ShortScheme extends Model {

	
	  @Id
	  @Constraints.Min(10)
	  public Long id;
	  
	  @Constraints.Required
	  public String name;
	  
	  public String description;
	  
	  public String longUrlPattern;
	  
	  public boolean done;
	  
	  @Formats.DateTime(pattern="dd/MM/yyyy")
	  public Date dueDate = new Date();
	  
	  public static Finder<Long,ShortScheme> find = new Finder<Long,ShortScheme>(
	    Long.class, ShortScheme.class
	  ); 
	
}
