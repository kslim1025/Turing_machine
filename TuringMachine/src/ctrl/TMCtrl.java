package ctrl;

import java.awt.Color;
import java.awt.Point;

import javax.swing.JOptionPane;
import javax.swing.JViewport;

import view.TMView;
import view.Tape;
import data.Machine;
import data.Transition;

public class TMCtrl{

	private TMView view;
	private Machine data;
	private Tape tape;
	/**
	 * Indicates the position of the reading head on the tape
	 */
	private int lect;
	/**
	 * The state of the program in a given moment
	 */
	private String currentState;
	private Character currentChar;
	private Transition currentTrans;
	/**
	 * True if the machine is in accept or reject state
	 */
	public boolean ended;
	/**
	 * True if the machine is already started and not finished
	 */
	public boolean started;
	/**
	 * True if the machine is running or paused
	 */
	public boolean running;
	/**
	 * True if the machine must run in 'stop mode'
	 */
	public boolean stop;
	private boolean ready;
	private JViewport vport;
	
	public TMCtrl(TMView v, Machine d){
		view = v;
		data = Machine.getInstance();
		lect = 0;
		ended = false;
		started = false;
		running = false;
		stop = false;
		tape = view.getTapePanel();
		currentState = data.getInitState();
		vport = view.getScrollTape().getViewport();
	}
	
	/* --- Actions --- */
	
	/**
	 * Launch the machine. Don't stop before end.
	 */
	public void startButton(boolean st){
		stop = st;
		if(ready){
			started = true;
			running = true;
			view.getButStart().setEnabled(false);
			new Thread(){
	            public void run(){
	            	while(!ended && running){
	        			TMCtrl.this.doTransition();
	        			try {
							Thread.sleep(300); //Can define a speed
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	        		}
	            	return;
	            }
	        }.start();
		}
	}
	
	/**
	 * Do a single transition
	 */
	public void startStepButton(){
		started = true;
		if(!ended){
			TMCtrl.this.doTransition();
		}
	}
	
	/**
	 * Stop the running program
	 */
	public void stopButton(){
		running = false;
		view.getButStart().setEnabled(true);
	}
	
	/**
	 * Launch the machine until a stop state
	 */
	public void step2Button(){
		this.startButton(true);
	}
	
	public void resetButton(){
		view.getInputField().setText("");
		tape.reset();
		view.getTable().getSelectionModel().clearSelection();
		view.getStateLabel().setBackground(Color.WHITE);
		view.getStateLabel().setForeground(Color.BLACK);
		view.setStateLabel("State");
		this.end();
	}
	
	
	/**
	 * Do the next transition of the machine
	 */
	private void doTransition(){
		//Scroll the panel to the head
		if(lect >= 9){
			vport.setViewPosition(new Point(lect*30-8*30,0));
		}
		
		currentChar = tape.getChar(lect);
		currentTrans = data.getTransitionFromSym(currentChar, currentState);
		
		tape.setDefaultColor(lect);
		//Overwrite the new symbole
		tape.setChar(lect, currentTrans.getNewSymbole());
		//Set the direction of the head
		if(currentTrans.getDirection().equals(">")) 
			lect++;
		else if(lect > 0)
			lect--;
		//Color the head
		tape.setHead(lect);
		
		String nextS = currentTrans.getNextState();
		if(data.isAccept(nextS)){
			view.setStateLabel(nextS);
			view.getStateLabel().setBackground(new Color(0x00D915));
			view.getStateLabel().setForeground(Color.WHITE);
			JOptionPane.showMessageDialog(view, "Etat acceptant !");
			this.end();
		}
		else if(data.isReject(nextS)){
			view.setStateLabel(nextS);
			view.getStateLabel().setBackground(Color.RED);
			view.getStateLabel().setForeground(Color.WHITE);
			JOptionPane.showMessageDialog(view, "Etat rejetant !");
			this.end();
		}
		else if(stop && data.isStop(nextS)){
			this.stopButton();
		}
		else{
			//Set the new state
			currentChar = tape.getChar(lect);
			currentState = currentTrans.getNextState();
			currentTrans = data.getTransitionFromSym(currentChar, currentState);
			view.getTable().setRowSelectionInterval(data.getTrans().indexOf(currentTrans), data.getTrans().indexOf(currentTrans));
			view.setStateLabel(currentState);
		}
	}
	
	

	/**
	 * Setup the tape with the given string in the field and the first state
	 * This field can't be empty
	 */
	public boolean init(){
		boolean ret = true;
		//Set the scroll bar to the left born
		if(vport.getViewPosition().getX() > 0)
			vport.setViewPosition(new Point(0,0));
		//State in black
		view.getStateLabel().setForeground(Color.BLACK);
		
		String input = view.getInputField().getText();
		//Tape input can't be empty
		if(!input.equals("")){
			tape.initTape(input);
			lect = 0;
			ended = false;
			currentState = data.getInitState();
			currentChar = tape.getChar(lect);
			currentTrans = data.getTransitionFromSym(currentChar, currentState);
			
			view.getStateLabel().setBackground(Color.white);
			view.setStateLabel(currentState);
			view.getTable().setRowSelectionInterval(data.getTrans().indexOf(currentTrans), data.getTrans().indexOf(currentTrans));
			ready = true;
		}
		else {
			JOptionPane.showMessageDialog(view, "Veuillez inscrire dans le champ ci-dessous le ruban initial.");
			ready = false;
		}
		return ret;

	}
	
	/**
	 * Reset the machine at the begining, ready to start again.
	 */
	public void end(){
		ended = true;
		started = false;
		running = false;
		stop = false;
		view.getButStart().setEnabled(true);
	}
	/* --------------- */
	
	
	public Tape getTape(){
		return tape;
	}
	
	public Machine getMachine(){
		return data;
	}
}
