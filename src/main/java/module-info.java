module cz.cvut.fel.pjv.mosteji1.poker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens cz.cvut.fel.pjv.mosteji1.poker to javafx.fxml;
    exports cz.cvut.fel.pjv.mosteji1.poker;
}