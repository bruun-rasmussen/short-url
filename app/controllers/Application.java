package controllers;

import models.ShortUrlTag;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	  public static Result index() {
	    return ok(index.render("Your new application is ready."));
	  }

	  public static Result shortTag(String tag) {
        ShortUrlTag url = ShortUrlTag.find.byId(tag);
        if (url == null)
            return notFound(tag);
	    return ok(url.toString()); // TODO; //ok("TBW! ["+tag+"] -> ?");
	  }

}