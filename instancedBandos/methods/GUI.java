package instancedBandos.methods;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import api.Variables;
import instancedBandos.data.Constants;
import simple.robot.api.ClientContext;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtHostName;

	public GUI() {
		setTitle("GUI");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 281, 173);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnStart = new JButton("Start");
		btnStart.setBounds(66, 94, 108, 23);
		contentPane.add(btnStart);

		txtHostName = new JTextField();
		txtHostName.setText("");
		txtHostName.setBounds(102, 8, 108, 20);
		contentPane.add(txtHostName);
		txtHostName.setColumns(10);

		JLabel lblNewLabel = new JLabel("Host name:");
		lblNewLabel.setBounds(22, 11, 70, 14);
		contentPane.add(lblNewLabel);

		JCheckBox chckbxTanking = new JCheckBox("Tanking");
		chckbxTanking.setBounds(6, 64, 77, 23);
		contentPane.add(chckbxTanking);

		JCheckBox chckbxSafeSpot = new JCheckBox("Use Safespot");
		chckbxSafeSpot.setBounds(130, 38, 129, 23);
		chckbxSafeSpot.setSelected(true);
		contentPane.add(chckbxSafeSpot);

		JCheckBox chckbxInsurance = new JCheckBox("Buy Insurance");
		chckbxInsurance.setBounds(6, 38, 122, 23);
		contentPane.add(chckbxInsurance);

		JCheckBox chckbxHosting = new JCheckBox("Hosting");
		chckbxHosting.setBounds(130, 64, 97, 23);
		contentPane.add(chckbxHosting);

		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Constants.TANKING = chckbxTanking.isSelected();
				Constants.USE_SAFESPOT = chckbxSafeSpot.isSelected();
				Constants.USE_INSURANCE = chckbxInsurance.isSelected();
				Constants.IS_HOST = chckbxHosting.isSelected();
				Constants.HOST_NAME = txtHostName.getText().trim();

				Variables.STARTED = true;
				dispose();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(contentPane, "Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					ClientContext.instance().stopScript();
				}
			}
		});
	}
}
