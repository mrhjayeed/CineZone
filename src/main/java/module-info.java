module com.example.movieticket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;

    // Jackson dependencies for JSON serialization in socket communication
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    opens com.example.movieticket to javafx.fxml;
    opens com.example.movieticket.controller to javafx.fxml;
    opens com.example.movieticket.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.example.movieticket.network to com.fasterxml.jackson.databind;

    exports com.example.movieticket;
    exports com.example.movieticket.controller;
    exports com.example.movieticket.model;
    exports com.example.movieticket.service;
    exports com.example.movieticket.network;
}