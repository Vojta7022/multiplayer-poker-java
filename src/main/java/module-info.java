module cz.cvut.fel.pjv.mosteji1.poker {
    requires javafx.controls;
    requires javafx.fxml;


    opens cz.cvut.fel.pjv.mosteji1.poker to javafx.fxml;
    exports cz.cvut.fel.pjv.mosteji1.poker;
}