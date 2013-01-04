package controllers;

import java.io.UnsupportedEncodingException;
import java.util.List;
import models.ShortScheme;
import models.ShortUrlTag;
import play.cache.Cached;
import play.data.Form;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result updateScheme(Long id) {
        Form<ShortScheme> form = form(ShortScheme.class).bindFromRequest();

        if (form.hasErrors())
            return badRequest(edit.render(id, form));

        form.get().update(id);
        flash("success", "URL Scheme " + form.get().name + " has been updated");

        return redirect(routes.Application.schemeList());
    }

    public static Result editScheme(Long id) {
        Form<ShortScheme> form =
                form(ShortScheme.class).fill(ShortScheme.find.byId(id));
        return ok(edit.render(id, form));
    }

    public static Result createScheme() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result schemeList() {
        List<ShortScheme> all = ShortScheme.find.orderBy().asc("name").findList();

        return ok(list.render(all));
    }

    @Cached(key = "tag")
    public static Result shortTag(String tag) {
        ShortUrlTag url = ShortUrlTag.find.byId(tag);
        if (url == null)
            return notFound(pageNotFound.render(tag));

        ShortScheme scheme = url.scheme;
        String target = url.target;

        String rep = scheme.expandTarget(target);

        return redirect(rep);
    }

    public static Result putShortTag(String schemeName, String target) throws UnsupportedEncodingException {
        // 1) Locate the short URL scheme:
        ShortScheme sch = ShortScheme.find.where().eq("name", schemeName).findUnique();
        if (sch == null)
            return notFound(pageNotFound.render(schemeName));

        // 2) Check if this target has a shortened tag already:
        ShortUrlTag shortcut = ShortUrlTag.find.where()
                .eq("scheme", sch)
                .eq("target", target).findUnique();

        if (shortcut == null) {
            shortcut = new ShortUrlTag();
            shortcut.scheme = sch;
            shortcut.tag = sch.generateTag();
            shortcut.target = target;
            shortcut.save();
        }

        return created(sch.tagPrefix + java.net.URLEncoder.encode(shortcut.tag, "UTF-8"));
    }

}
