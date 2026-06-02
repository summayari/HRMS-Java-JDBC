package hrms.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Handles profile photo loading, resizing and placeholder generation. */
public class PhotoHelper {

    /**
     * Loads and returns a circular profile image from the given path.
     * Falls back to a coloured initial avatar if path is null/invalid.
     */
    public static ImageIcon loadAvatar(String photoPath, String initials, Color bgColor, int size) {
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                BufferedImage img = ImageIO.read(new File(photoPath));
                if (img != null) {
                    return makeCircularIcon(img, size);
                }
            } catch (Exception ignored) {}
        }
        return makeInitialsIcon(initials, bgColor, size);
    }

    /** Opens a file chooser and returns the selected image path, or null. */
    public static String pickPhoto(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Profile Photo");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    /** Copies a photo to the app's photos directory and returns the new path. */
    public static String savePhoto(String sourcePath, int empId) {
        try {
            File photosDir = new File("employee_photos");
            if (!photosDir.exists()) photosDir.mkdirs();
            String ext = sourcePath.substring(sourcePath.lastIndexOf('.'));
            File dest = new File(photosDir, "emp_" + empId + ext);
            // Copy file
            try (InputStream in = new FileInputStream(sourcePath);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            return dest.getAbsolutePath();
        } catch (Exception ex) {
            ex.printStackTrace();
            return sourcePath; // fall back to original path
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static ImageIcon makeCircularIcon(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
        // Scale image to fit
        g2.drawImage(src.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return new ImageIcon(out);
    }

    private static ImageIcon makeInitialsIcon(String initials, Color bg, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.WHITE);
        String txt = initials.length() > 2 ? initials.substring(0, 2).toUpperCase() : initials.toUpperCase();
        int fontSize = size / 3;
        g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(txt)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(txt, x, y);
        g2.dispose();
        return new ImageIcon(out);
    }
}
