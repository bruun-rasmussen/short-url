package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import models.ShortScheme;
import models.ShortUrlTag;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
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
    public static Result redirectTag(String tag) {
        ShortUrlTag url = ShortUrlTag.find.byId(tag);
        if (url == null)
            return notFound(pageNotFound.render(tag));

        ShortScheme scheme = url.scheme;
        String target = url.target;

        String rep = scheme.expandTarget(target);

        return redirect(rep);
    }

    public static Result shortTag(String schemeName, String target) throws IOException, WriterException {

        ShortUrlTag shortcut = findOrCreateShortcut(schemeName, target);
        String shortUrl = shortcut.getShortcutUrl();
        byte png[] = qrCode(shortUrl, null, null, null);
        String qrBase64 = new String(Base64.encodeBase64(png));
    //  return ok(qrBase64).as("text/plain");
        return ok(viewTag.render(shortcut, shortUrl, qrBase64));
    }

    public static Result shortTagQR(String schemeName, String target, Integer size, Integer margin, String ecc) throws IOException, WriterException {
        ShortUrlTag shortcut = findOrCreateShortcut(schemeName, target);
        String shortUrl = shortcut.getShortcutUrl();
        byte png[] = qrCode(shortUrl, size, margin, ecc);
        Logger.info("PNG (size: " + png.length+ " bytes)");
        return ok(png).as("image/png");
    }

    private static byte[] qrCode(String shortUrl, Integer size, Integer margin, String ecc) throws IOException, WriterException {
        if (size == null)
            size = 256;
        if (margin == null)
            margin = 0;
        if (ecc == null)
            ecc = "M";

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION,
                "L".equals(ecc) ? ErrorCorrectionLevel.L :
                "M".equals(ecc) ? ErrorCorrectionLevel.M :
                "Q".equals(ecc) ? ErrorCorrectionLevel.Q :
                "H".equals(ecc) ? ErrorCorrectionLevel.H : ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, margin);

        BitMatrix matrix = writer.encode(shortUrl, BarcodeFormat.QR_CODE, size, size, hints);
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", bo);
        return bo.toByteArray();
    }

    public static Result putShortTag(String schemeName, String target) {
        ShortUrlTag shortcut = findOrCreateShortcut(schemeName, target);
        return created(shortcut.getShortcutUrl());
    }

    private static ShortUrlTag findOrCreateShortcut(String schemeName, String target) {
        // 1) Locate the short URL scheme:
        ShortScheme sch = ShortScheme.find.where().eq("name", schemeName).findUnique();
        if (sch == null)
            throw new IllegalArgumentException(schemeName + ": no such URL scheme");

        // 2) Check if this target has a shortened tag already:
        ShortUrlTag shortcut = ShortUrlTag.find.where()
                .eq("scheme", sch)
                .eq("target", target).findUnique();

        if (shortcut == null) {
            if (!sch.accepts(target))
                throw new IllegalArgumentException(target + ": invalid shortcut target");

            shortcut = new ShortUrlTag();
            shortcut.scheme = sch;
            shortcut.tag = sch.generateTag();
            shortcut.target = target;
            shortcut.save();

            Logger.info(shortcut + ": shortcut created");
        }
        return shortcut;
    }
}
