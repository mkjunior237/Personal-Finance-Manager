import com.financemanager.PersonalFinanceManager;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersonalFinanceManager app = new PersonalFinanceManager();
            app.setVisible(true);
        });
    }
}