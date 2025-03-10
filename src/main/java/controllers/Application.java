package controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import com.google.gson.Gson;
import models.ShortScheme;
import models.ShortUrlTag;

@Slf4j
@Path("/")
public class Application {

    @GET
    @Path("{tag}")
    @Produces("application/json")
    public Response redirectTag(@PathParam("tag") String tag,
                                @QueryParam("info") @DefaultValue("false") boolean showInfo) {
        ShortUrlTag shortcut = ShortUrlTag.findByTag(tag);
        if (shortcut == null) {
            log.info("'{}': tag not found", tag);
            return Response.status(Status.NOT_FOUND).entity("'" + tag + "': not found").build();
        }

        if (showInfo) {
            log.info("'{}': info {}", tag, shortcut);            
            return Response.ok(shortcut).build();
        }
        else {
            URI rep = shortcut.getTargetURI();
            log.info("'{}' \u279d {}", tag, rep);
    
            return Response.seeOther(rep).build();    
        }
    }

    @GET
    @Path("/{scheme}/{target}-qr.png")
    @Produces("image/png")
    public Response getQRCodeAsPNG(@PathParam("scheme") String schemeName, 
                                   @PathParam("target") String target,
                                   @QueryParam("size") @DefaultValue("256") Integer size,
                                   @QueryParam("margin") @DefaultValue("0") Integer margin,
                                   @QueryParam("ecc") @DefaultValue("M") String ecc) throws WriterException, IOException 
    {
        ShortScheme scheme = ShortScheme.find("name", schemeName).firstResult();
        if (scheme == null)
            return Response.status(Status.BAD_REQUEST).entity("'" + schemeName + "': unrecognized URL scheme").build();

        ShortUrlTag shortcut = ShortUrlTag.findOrCreate(scheme, target);
        String shortUrl = shortcut.getShortcutUrl();

        byte png[] = getQRCodeAsPNG(shortUrl, size, margin, ecc);
        log.info("{}/{}-qr.png ({} bytes): QR code for {} served", schemeName, target, png.length, shortUrl);
        return Response.ok(png).build();
    }

    private byte[] getQRCodeAsPNG(String shortUrl, int size, int margin, String ecc) throws WriterException, IOException {  

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
}
