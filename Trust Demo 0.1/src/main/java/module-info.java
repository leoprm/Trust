module com.mycompany.trust {
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.jakartaee.api;
    requires transitive javafx.graphics; // Added transitive modifier

    opens com.mycompany.trust to javafx.fxml;
    exports com.mycompany.trust;
}
