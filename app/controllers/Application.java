package controllers;

import models.ShortScheme;
import models.ShortUrlTag;
import play.cache.Cached;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	  public static Result index() {
	    return ok(index.render("Your new application is ready."));
	  }

      @Cached(key="tag")
	  public static Result shortTag(String tag) {
        ShortUrlTag url = ShortUrlTag.find.byId(tag);
        if (url == null)
            return notFound(pageNotFound.render(tag));

        ShortScheme scheme = url.scheme;
        String target = url.target;

        String rep = scheme.expandTarget(target);

	    return redirect(rep);
	  }

}