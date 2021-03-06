package org.lpw.tephra.util;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author lpw
 */
@Component("tephra.util.io")
public class IoImpl implements Io {
    private static final int BUFFER_SIZE = 1024;

    @Inject
    private Logger logger;
    @Inject
    private Validator validator;

    @Override
    public void mkdirs(File file) {
        if (file == null)
            return;

        if (!file.mkdirs())
            logger.warn(null, "创建目录（集）[{}]失败！", file.getAbsolutePath());
        else if (logger.isDebugEnable())
            logger.debug("成功创建目录（集）[{}]。", file.getAbsolutePath());
    }

    @Override
    public void mkdirs(String path) {
        mkdirs(new File(path));
    }

    @Override
    public byte[] read(String path) {
        if (validator.isEmpty(path) || !new File(path).exists())
            return null;

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream input = new FileInputStream(path);
            copy(input, output);
            input.close();
            output.close();

            return output.toByteArray();
        } catch (IOException e) {
            logger.warn(e, "读文件[{}]时异常！", path);

            return null;
        }
    }

    @Override
    public void write(String path, byte[] content) {
        if (validator.isEmpty(path))
            return;

        try {
            if (logger.isDebugEnable())
                logger.debug("写入文件：{}", path);

            OutputStream output = new FileOutputStream(path);
            output.write(content);
            output.close();
        } catch (IOException e) {
            logger.warn(e, "写入文件[{}]时异常！", path);
        }
    }

    @Override
    public void copy(String input, String output) {
        try {
            OutputStream outputStream = new FileOutputStream(output);
            InputStream inputStream = new FileInputStream(input);
            copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            logger.warn(e, "复制文件[{}->{}]时发生异常！", input, output);
        }
    }

    @Override
    public void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int length; (length = input.read(buffer)) > -1; )
            output.write(buffer, 0, length);
        output.flush();
    }

    @Override
    public void write(OutputStream output, StringBuffer source) throws IOException {
        if (source == null || source.length() == 0)
            return;

        Writer writer = new OutputStreamWriter(output);
        for (int i = 0, length = source.length(); i < length; i++)
            writer.append(source.charAt(i));
        writer.flush();
    }

    @Override
    public void move(File source, File target) throws IOException {
        OutputStream outputStream = new FileOutputStream(target);
        InputStream inputStream = new FileInputStream(source);
        copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
        delete(source);
    }

    @Override
    public void move(String source, String target) throws IOException {
        move(new File(source), new File(target));
    }

    @Override
    public void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files)
                    delete(f);
        }
        if (!file.delete())
            logger.warn(null, "删除文件/目录[{}]失败！", file.getAbsolutePath());
        else if (logger.isDebugEnable())
            logger.debug("成功删除文件/目录[{}]。", file.getAbsolutePath());
    }

    @Override
    public void delete(String path) {
        delete(new File(path));
    }
}
