package org.feup.cpd.interfaces;

import java.io.File;

public interface KeyValue {
    void put(String key, String value);
    void get(String key);
    void delete(String key);
}
