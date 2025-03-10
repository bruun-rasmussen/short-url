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
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
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
        String url = getShortUrl(schemeName, target);
        BitMatrix qr = getQRMatrix(url, size, margin, ecc);        
        byte[] png = toPngBytes(qr);

        log.info("{}/{}-qr.png ({} bytes): QR code for {} served", schemeName, target, png.length, url);
        return Response.ok(png).build();
    }

    @GET
    @Path("/{scheme}/{target}-qr.svg")
    @Produces("image/svg+xml")
    public Response getQRCodeAsSVG(@PathParam("scheme") String schemeName, 
                                   @PathParam("target") String target,
                                   @QueryParam("scale") @DefaultValue("4") Integer scale,
                                   @QueryParam("margin") @DefaultValue("0") Integer margin,
                                   @QueryParam("ecc") @DefaultValue("M") String ecc) throws WriterException, IOException 
    {
        String url = getShortUrl(schemeName, target);
        BitMatrix qr = getQRMatrix(url, 0, margin, ecc);        
        String svg = toSvg(qr, scale);

        log.info("{}/{}-qr.svg ({} chars): QR code for {} served", schemeName, target, svg.length(), url);
        return Response.ok(svg).build();
    }

    private String getShortUrl(String schemeName, String target) {
        ShortScheme scheme = ShortScheme.find("name", schemeName).firstResult();
        if (scheme == null)
            throw new BadRequestException("'" + schemeName + "': unrecognized URL scheme");

        ShortUrlTag shortcut = ShortUrlTag.findOrCreate(scheme, target);
        return shortcut.getShortcutUrl();
    }

    private BitMatrix getQRMatrix(String shortUrl, int size, int margin, String ecc) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION,
                "L".equals(ecc) ? ErrorCorrectionLevel.L :
                "M".equals(ecc) ? ErrorCorrectionLevel.M :
                "Q".equals(ecc) ? ErrorCorrectionLevel.Q :
                "H".equals(ecc) ? ErrorCorrectionLevel.H : ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, margin);

        BitMatrix matrix = writer.encode(shortUrl, BarcodeFormat.QR_CODE, size, size, hints);
        return matrix;
    }

    private static byte[] toPngBytes(BitMatrix matrix) throws IOException {
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", bo);
        return bo.toByteArray();
    }

    private static String toSvg(BitMatrix bitMatrix, int scale) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 ").append(width*scale).append(" ").append(height*scale).append("\" stroke=\"none\">\n");
        sb.append("  <path class=\"qr-code\" d=\"");

        BitArray row = new BitArray(width);
        for(int y = 0; y < height; y++) {
            row = bitMatrix.getRow(y, row);
            for(int x = 0; x < width; x++) 
                if (row.get(x)) 
                    sb.append(" M").append(x*scale).append(",").append(y*scale)
                      .append("h").append(scale).append("v").append(scale).append("h").append(-scale).append("z");
        }        

        sb.append(" \"/>\n");
        sb.append("</svg>\n");        

        return sb.toString();
    }
}
