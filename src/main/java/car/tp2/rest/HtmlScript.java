package car.tp2.rest;

public class HtmlScript {

    public static String LINK_CSS = "<style>\n" +
            "a:link {\n" +
            "    color: green;\n" +
            "    margin : 10px ;\n " +
            "    background-color: transparent;\n" +
            "    text-decoration: none;\n" +
            "}\n" +
            "a:hover {\n" +
            "    color: red;\n" +
            "    background-color: transparent;\n" +
            "    text-decoration: underline;\n" +
            "}\n" +
            "a:active {\n" +
            "    color: yellow;\n" +
            "    background-color: transparent;\n" +
            "    text-decoration: underline;\n" +
            "}\n" +
            "</style>";
    public static String LOGIN_SCRIPT = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<a class=\"button postfix login\" href=\"#\" \n" +
            ">Sign in</a> \n" +
            "\n" +
            "\n" +
            "<script>\n" +
            "var username = \"brahimftp\";\n" +
            "var password = \"sam1\";\n" +
            "\n" +
            "function make_base_auth(user, password) {\n" +
            "  var tok = user + ':' + password;\n" +
            "  var hash = btoa(tok);\n" +
            "  return \"Basic \" + hash;\n" +
            "}\n" +
            "\n" +
            "$(\".button.postfix.login\").on(\"click\", function(){\n" +
            "alert('avant ajax');\n" +
            "$.ajax\n" +
            "  ({\n" +
            "    type: \"GET\",\n" +
            "    url: \"http://localhost:9988/myapp/myresource/listHt/\",\n" +
            "    dataType: 'json',\n" +
            "    async: false,\n" +
            "    data: '{}',\n" +
            "    beforeSend: function (xhr){ \n" +
            "        xhr.setRequestHeader('Authorization', make_base_auth(username, password)); \n" +
            "    },\n" +
            "    success: function (){\n" +
            "        alert('Thanks for your comment!'); \n" +
            "    }\n" +
            "\n" +
            "    error: function()\n" +
            "    {\n" +
            "      alert('Error');\n" +
            "    }\n" +
            "});\n" +
            "\n" +
            "alert(\"apres ajax\");\n" +
            "});\n" +
            "\n" +
            "\n" +
            "</body>\n" +
            "</html>\n";
}
