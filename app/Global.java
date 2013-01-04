import play.*;
import play.libs.*;

import java.util.*;

import com.avaje.ebean.*;

import models.*;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        InitialData.insert(app);
    }

    static class InitialData {

        public static void insert(Application app) {
            if (Ebean.find(ShortUrlTag.class).findRowCount() == 0) {
                Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yml");

                Ebean.save(all.get("schemes"));
                Ebean.save(all.get("tags"));
            }
        }
    }
}
