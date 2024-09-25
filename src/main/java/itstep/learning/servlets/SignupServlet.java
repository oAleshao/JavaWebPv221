package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;
import itstep.learning.models.formmodels.SignupModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;


@Singleton
public class SignupServlet extends HttpServlet {

    private final FormParseService formParseService;
    private final FileService fileService;
    private final UserDao userDao;
    private final Logger logger;

    @Inject
    public SignupServlet(@Named("formParse") FormParseService formParseService, FileService fileService, UserDao userDao, Logger logger) {
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.userDao = userDao;
        this.logger = logger;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getMethod().toUpperCase()) {
            case "PATCH":
                doPatch(req, resp);
                break;
            default:
                super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userLogin = req.getParameter("user-email");
        String userPassword = req.getParameter("user-password");
        logger.info("User login: " + userLogin + " password: " + userPassword);
        RestResponse restResponse = new RestResponse();
        resp.setContentType("application/json");
        Gson gson = new GsonBuilder().serializeNulls().create();

        if (userLogin == null || userLogin.isEmpty() || userPassword == null || userPassword.isEmpty()) {
            restResponse.setStatus("Error");
            restResponse.setData("Missing or empty credentials");
            resp.getWriter().write(gson.toJson(restResponse));
            return;
        }

        User user = userDao.authenticate(userLogin, userPassword);
        if (user == null) {
            restResponse.setStatus("Error");
            restResponse.setData("Invalid credentials");
            resp.getWriter().write(gson.toJson(restResponse));
            return;
        }


        // утримання авторизації - сесія
        // зберігаємо у сесія відомості про користувача
        HttpSession session = req.getSession();
        session.setAttribute("userId", user.getId());

        restResponse.setStatus("OK");
        restResponse.setData(user);
        resp.getWriter().print(gson.toJson(restResponse));


    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("page", "signup");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse = new RestResponse();
        resp.setContentType("application/json");

        SignupModel model;
        try {
            model = getModelFromRequest(req);
        } catch (Exception ex) {
            restResponse.setStatus("Error");
            restResponse.setData(ex.getMessage());
            resp.getWriter().print(new Gson().toJson(restResponse));
            return;
        }

        User user = userDao.signup(model);
        if (user == null) {
            restResponse.setStatus("Error");
            restResponse.setData("500 DB Error, details on server logs");
            resp.getWriter().print(new Gson().toJson(restResponse));
            return;
        }

        restResponse.setStatus("Ok");
        restResponse.setData(model);

        resp.getWriter().print(new Gson().toJson(restResponse));

    }

    private SignupModel getModelFromRequest(HttpServletRequest request) throws Exception {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd-MM-yyyy");

        FormParseResult res = formParseService.parse(request);

        SignupModel model = new SignupModel();
        model.setName(res.getFields().get("user_name"));
        if (model.getName() == null || model.getName().isEmpty()) {
            throw new Exception("user_name - is required");
        }


        model.setEmail(res.getFields().get("user_email"));
        if (model.getEmail() == null || model.getEmail().isEmpty()) {
            throw new Exception("user_email - is required");
        }


        try {
            model.setBirthday(dateParser.parse(
                    res.getFields().get("user_birth")
            ));
        } catch (ParseException e) {
            throw new Exception(e.getMessage());
        }

        String uploadedName = null;
        FileItem avatar = res.getFiles().get("user_avatar");
        if (res.getFiles().get("user_avatar").getSize() > 0) {
            uploadedName = fileService.upload(avatar);
            model.setAvatar(uploadedName);
        }
        System.out.println(uploadedName);

        model.setPassword(res.getFields().get("user_pass"));

        return model;
    }


}


/*
* Утримання авторизації - забезпечення часового проміжку протягом якого
* не перезапитуються парольні дані.
*
* Схеми
* - за токенами (розподілена архітектурна бек/фронт):
*   при автентифікації видається токен
*   при запитах передається токен
*   401 - не автинтифікований користувач
*
* - за сесіми (серверними сесіями)
*   при афтентифікації стартує сесія
*   при запиті перевіряється сесія
*
* Токен(від англ. - жетон, посвідчення) - дані, що індетифікують їх
* власника
* Комунікація
* 1. Одержання токену (автентифікація)
*   GET /auth a)?login&password
*             b) Authorization: Basic login&password
*   -> token
* 2. Використання токену (авторизція)
*   GET /resource
*   Authorization: Bearer token
*
* */