package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.services.DepartmentService;
import models.exceptions.ValidationException;

public class DepartmentFormController implements Initializable {

	private Department entity;
	
	private DepartmentService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList();
	
	@FXML
	private TextField txtId;
	
	@FXML
	private TextField txtName;
	
	@FXML
	private Label labelErrorName;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	public void setDepartment(Department entity) {
		this.entity = entity;
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	//Adiciona esse listener em uma lista
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
		entity = getFormData();
		
		//Salva ou atualiza os dados no banco de dados
		service.saveOrUpdate(entity);
		notifyDataChangeListeners();
		
		//Fecha a janela do formulario e volta para a lista de departamentos
		Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErros());
		}
		
		catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
		
	}
	
	//Aciona o evento na DepartmentListontroller para atualizar a table view
	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	//Passa os dados dos textField para o objeto que vai ser salvo ou alterado
	private Department getFormData() {
		Department obj = new Department();
		ValidationException exception = new ValidationException("Validation error");
		obj.setId(Utils.tryParseToInt(txtId.getText()));
		
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(txtName.getText());
		
		if (exception.getErros().size() > 0) {
			throw exception;
		}
		
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
		
	}
	
	//Inicializa os nodes quando entra tela limitando os text field
	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
	}
	
	
	//Coloca os dados do objeto carregado na lista nos text field
	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
	}
	
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		if (fields.contains("name")) {
			labelErrorName.setText(errors.get("name"));
		}
	}
}
