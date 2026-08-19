module com.papel.imdb_clone {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.media;
    requires transitive javafx.web;
    requires transitive org.controlsfx.controls;
    requires transitive org.slf4j;
    requires java.logging;
    requires java.net.http;
    requires java.desktop;
    requires java.sql;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.annotation;

    // Main module opens
    opens com.papel.imdb_clone to javafx.fxml, com.fasterxml.jackson.databind, javafx.base;
    
    // Controller packages
    opens com.papel.imdb_clone.controllers to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.controllers.people to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.controllers.content to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.controllers.authentication to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.controllers.search to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.controllers.coordinator to javafx.fxml, javafx.base, com.fasterxml.jackson.databind;
    
    // Model packages - only open specific model packages that exist
    opens com.papel.imdb_clone.model.content to com.fasterxml.jackson.databind, javafx.base, javafx.fxml;
    opens com.papel.imdb_clone.model.people to com.fasterxml.jackson.databind, javafx.base, javafx.fxml;
    opens com.papel.imdb_clone.model.rating to com.fasterxml.jackson.databind, javafx.base, javafx.fxml;

    // Service packages - only open existing subpackages
    opens com.papel.imdb_clone.service.people to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.content to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.validation to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.search to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.navigation to javafx.base, com.fasterxml.jackson.databind, javafx.fxml;
    opens com.papel.imdb_clone.service.data.base to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.data.loader to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.data.loader.content to javafx.base, com.fasterxml.jackson.databind;
    opens com.papel.imdb_clone.service.data.loader.people to javafx.base, com.fasterxml.jackson.databind;
    
    // Util and other packages
    opens com.papel.imdb_clone.util to javafx.base;
    opens com.papel.imdb_clone.gui to javafx.fxml, javafx.base;
    opens com.papel.imdb_clone.data to javafx.base, com.fasterxml.jackson.databind;
    
    // Enums
    opens com.papel.imdb_clone.enums to javafx.base, com.fasterxml.jackson.databind;
    
    // Exports - only export packages that actually exist
    exports com.papel.imdb_clone;
    exports com.papel.imdb_clone.controllers;
    exports com.papel.imdb_clone.controllers.people;
    exports com.papel.imdb_clone.controllers.content;
    exports com.papel.imdb_clone.controllers.authentication;
    exports com.papel.imdb_clone.controllers.search;
    exports com.papel.imdb_clone.gui;
    exports com.papel.imdb_clone.model.content;
    exports com.papel.imdb_clone.model.people;
    exports com.papel.imdb_clone.model.rating;
    // Service exports - only export existing subpackages
    exports com.papel.imdb_clone.service.people;
    exports com.papel.imdb_clone.service.content;
    exports com.papel.imdb_clone.service.validation;
    exports com.papel.imdb_clone.service.search;
    exports com.papel.imdb_clone.service.navigation;
    exports com.papel.imdb_clone.service.data.base;
    exports com.papel.imdb_clone.service.data.loader;
    exports com.papel.imdb_clone.service.data.loader.content;
    exports com.papel.imdb_clone.service.data.loader.people;
    exports com.papel.imdb_clone.util;
    exports com.papel.imdb_clone.data;
    exports com.papel.imdb_clone.enums;
    exports com.papel.imdb_clone.exceptions;
    exports com.papel.imdb_clone.repository;
    exports com.papel.imdb_clone.repository.impl;
    
    // Add additional exports for other packages that need reflection
    exports com.papel.imdb_clone.controllers.coordinator to javafx.fxml;
}