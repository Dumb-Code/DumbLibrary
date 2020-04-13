package net.dumbcode.dumblibrary.client.gui.filebox;

import org.lwjgl.opengl.Display;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

//TODO: convert to swing
public class FileDropboxFrame extends Frame {

    public FileDropboxFrame(String title, @Nullable FilenameFilter filter, Consumer<File> onFile) {
        super(title);

        Predicate<Transferable> runTransferable = transferable -> {
            try {
                if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if(files.size() == 1) {
                        File file = files.get(0);
                        if(filter == null || filter.accept(file.getParentFile(), file.getName())) {
                            onFile.accept(file);
                            dispose();
                            return true;
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                //Ignore exception
                e.printStackTrace();
            }
            return false;
        };

        if(runTransferable.test(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null))) {
           return;
        }


        int width = 300;
        int height = 200;

        this.setLocation(Display.getX() + (Display.getWidth() - width) / 2, Display.getY() + (Display.getHeight() - height) / 2);
        this.setSize(width, height);

        Label label = new Label("Drag or paste files, or click to open file dialog", Label.CENTER);
        this.add(label);

        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) { }

            @Override
            public void dragOver(DropTargetDragEvent dtde) { }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) { }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(dtde.getDropAction());
                runTransferable.test(dtde.getTransferable());
            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_V && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    runTransferable.test(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FileDialog dialog = new FileDialog(FileDropboxFrame.this);
                dialog.setVisible(true);
                dialog.setFilenameFilter(filter);
                File[] files = dialog.getFiles();
                if(files.length == 1) {
                    onFile.accept(files[0]);
                    dispose();
                }
                super.mouseClicked(e);
            }
        });

        this.setVisible(true);
        this.requestFocus();
        this.toFront();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                super.windowClosing(e);
            }
        });
    }

}
