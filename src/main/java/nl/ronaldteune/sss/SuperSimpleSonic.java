package nl.ronaldteune.sss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ronaldteune.sss.controller.ArtistAlbum;
import nl.ronaldteune.sss.controller.Database;
import nl.ronaldteune.sss.controller.SoundStream;
import nl.ronaldteune.sss.model.Directory;
import nl.ronaldteune.sss.model.Indexes;
import nl.ronaldteune.sss.model.MusicFolder;
import nl.ronaldteune.sss.model.MusicFolders;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class SuperSimpleSonic {
    public static final String MUSIC_PATH = "/var/lib/mpd/music";
    private static Database database = new Database();
    private static ArtistAlbum artistAlbum = new ArtistAlbum();

    public static void main(String[] args) throws SQLException {
        database.init(MUSIC_PATH);
        exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
        staticFiles.location("static");
        port(8080);
        get("/rest/ping.view", SuperSimpleSonic::ping);
        get("/rest/getMusicFolders.view", SuperSimpleSonic::getMusicFolders);
        get("/rest/getMusicDirectory.view", SuperSimpleSonic::getMusicDirectory);
        get("/rest/rest/getMusicDirectory.view", SuperSimpleSonic::getMusicDirectory);
        get("/rest/getRandomSongs.view", SuperSimpleSonic::getRandomSongs);
        get("/rest/getIndexes.view", SuperSimpleSonic::getIndexes);
        get("/rest/getAlbumList.view", SuperSimpleSonic::getAlbumList);
        get("/rest/getCoverArt.view", SuperSimpleSonic::getCoverArt);
        get("/rest/getLicense.view", SuperSimpleSonic::getLicense);
        get("/rest/stream.view", SuperSimpleSonic::stream);
        get("/*", (req, res) -> catchAll(req));
    }

    private static byte[] getCoverArt(Request req, Response res) {
        printRequestURL(req, "getCoverArt");
        res.type("image/jpeg");

        return artistAlbum.getCoverArt(req.queryParams("id"), database);
    }

    private static String getLicense(Request req, Response res) {
        printRequestURL(req, "getLicense");
        res.type("application/json");
        return "{\n" +
                "    \"subsonic-response\": {\n" +
                "        \"status\": \"ok\",\n" +
                "        \"version\": \"1.7.0\",\n" +
                "        \"license\": {\n" +
                "            \"valid\": true,\n" +
                "            \"email\": \"me@example.com\",\n" +
                "            \"key\": \"ABC123DEF\",\n" +
                "            \"date\": \"2009-09-03T14:46:43\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private static String createSubSonicOKResponse(Object object, String responseType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(object);
        return "{\"subsonic-response\": {\"status\": \"ok\", \"version\": \"1.7.0\", \"" + responseType + "\": " + jsonString + "}}";
    }

    private static String getAlbumList(Request req, Response res) {
        printRequestURL(req, "getAlbumList");
        res.type("application/json");
        return ""; // TODO
    }

    private static String getIndexes(Request req, Response res) throws JsonProcessingException {
        printRequestURL(req, "getIndexes");
        res.type("application/json");

        Indexes indexes = artistAlbum.getArtistList(database);

        return createSubSonicOKResponse(indexes, "indexes");
    }

    private static String getRandomSongs(Request req, Response res) {
        printRequestURL(req, "getRandomSongs");
        res.type("application/json");
        return ""; // TODO
    }

    private static String getMusicFolders(Request req, Response res) throws JsonProcessingException {
        printRequestURL(req, "getMusicFolders");
        res.type("application/json");

        var m = new MusicFolders();
        var mf = new MusicFolder("1", "test");
        m.setMusicFolder(Collections.singletonList(mf));

        return createSubSonicOKResponse(m, "musicFolders");
    }

    private static void printRequestURL(Request req, String vari) {
        System.out.println("http://localhost:8080/rest/" + vari + ".view?" + req.raw().getQueryString());
    }

    private static Object catchAll(Request req) {
        System.out.println("http://localhost:8080/rest/" + req.splat()[0] + "?" + req.raw().getQueryString());
        return null;
    }


    private static String ping(Request req, Response res) {
        System.out.println(req.raw().getQueryString());
        res.type("application/json");
        System.out.println("ping " + req.body());
        return "{\n" +
                "    \"subsonic-response\": {\n" +
                "        \"status\": \"ok\",\n" +
                "        \"version\": \"1.6.0\"\n" +
                "    }\n" +
                "}";
    }

    private static byte[] stream(Request request, Response response) throws IOException {
        printRequestURL(request, "stream");
        final String requestId = request.queryParams("id");
        final String path = artistAlbum.getStreamPath(requestId, database);

        try {
            return SoundStream.getSoundStream(MUSIC_PATH + path, response.raw().getOutputStream());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static String getMusicDirectory(Request req, Response res) throws JsonProcessingException {
        printRequestURL(req, "getMusicDirectory");
        Directory directory = artistAlbum.getAlbumsOrTracks(req, database);

        res.type("application/json");

        String fullResponse = createSubSonicOKResponse(directory, "directory");
        System.out.println(fullResponse);
        return fullResponse;
    }

}
