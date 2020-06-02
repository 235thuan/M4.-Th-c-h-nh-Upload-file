package upload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import upload.service.MyUploadFormImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MyFileUploadController {
    @Autowired
    MyUploadFormImpl myUploadForm = new MyUploadFormImpl();

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);
        if (target.getClass() == MyUploadFormImpl.class) {
            dataBinder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        }
    }

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("upload/index");
        return modelAndView;
    }

    @GetMapping("/uploadOneFile")
    public ModelAndView uploadOneFileHandler() {
        ModelAndView modelAndView = new ModelAndView("upload/uploadOneFile");
        modelAndView.addObject("myUploadForm", myUploadForm);
        return modelAndView;
    }

    @PostMapping("/uploadOneFile")
    public ModelAndView uploadOneFileHandlerPOST(
            HttpServletRequest request){
        return this.doUpload(request);
    }

    @GetMapping("/uploadMultiFile")
    public ModelAndView uploadMultiFileHandler() {
        ModelAndView modelAndView = new ModelAndView("upload/uploadMultiFile");
        modelAndView.addObject("myUploadForm", myUploadForm);
        return modelAndView;
    }

    @PostMapping("/uploadMultiFile")
//    public String uploadMultiFileHandlerPOST( HttpServletRequest request,Model model, @ModelAttribute MyUploadFormImpl myUploadForm){
    public ModelAndView uploadMultiFileHandlerPOST(
            HttpServletRequest request){
        return this.doUpload(request);
    }

    private ModelAndView doUpload(HttpServletRequest request) {
        String description = myUploadForm.getDescription();
        System.out.println("Description: " + description);
        String uploadRootPath = request.getServletContext().getRealPath("upload");
        System.out.println("uploadRootPath=" + uploadRootPath);
        File uploadRootDir = new File(uploadRootPath);
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }
        CommonsMultipartFile[] fileDatas = myUploadForm.getFileDatas();
        Map<File, String> uploadedFiles = new HashMap();
        for (CommonsMultipartFile fileData : fileDatas) {
            String name = fileData.getOriginalFilename();
            System.out.println("Client File Name = " + name);
            if (name != null && name.length() > 0) {
                try {
                    File serverFile = new File(uploadRootDir.getAbsolutePath()
                            + File.separator + name);
                    BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(serverFile));
                    stream.write(fileData.getBytes());
                    stream.close();
                    uploadedFiles.put(serverFile, name);
                    System.out.println("Write file: " + serverFile);
                } catch (Exception e) {
                    System.out.println("Error Write file: " + name);
                }
            }
        }
        ModelAndView modelAndView = new ModelAndView("upload/uploadResult");
        modelAndView.addObject("description", description);
        modelAndView.addObject("uploadedFiles", uploadedFiles);
        return modelAndView;
    }
}
