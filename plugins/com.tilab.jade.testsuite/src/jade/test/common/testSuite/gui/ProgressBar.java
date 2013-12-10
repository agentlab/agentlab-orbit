/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/
package jade.test.common.testSuite.gui;

import java.awt.Color;

/**
 *
 * @author  Ale
 */
public class ProgressBar extends javax.swing.JPanel implements java.io.Serializable {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar;

    private int currentValue;
    private Color statusColor;
    private boolean fError = false;
    
    
    /** Creates new form ProgressBar */
    public ProgressBar() {
        initComponents();
        jProgressBar.setStringPainted(true);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jProgressBar = new javax.swing.JProgressBar();

        setLayout(new java.awt.GridBagLayout());

        jProgressBar.setForeground(getStatusColor());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jProgressBar, gridBagConstraints);

    }//GEN-END:initComponents

    /** Getter for property statusColor.
     * @return Value of property statusColor.
     *
     */
    public Color getStatusColor() {
    	return statusColor;
    }    

    /** Setter for property statusColor.
     * @param statusColor New value of property statusColor.
     *
     */
    public void setStatusColor(Color c) {
        statusColor = c;
    }
    
    public void start(int total) {
        jProgressBar.setMaximum(total);
        reset();
    }
    
    public void reset() {
        currentValue = 0;
        fError = false;
        statusColor = Color.green;
        jProgressBar.setForeground(statusColor);
        jProgressBar.setValue(0);
    }

    public void step(boolean successful) {
    	currentValue++;
    	update(successful);
    }
    	
    public void setValue(int value, boolean successful) {
    	currentValue = value;
    	update(successful);
    }
    
    private void update(boolean successful) {
      jProgressBar.setValue(currentValue);
    	fError = fError || (!successful);
    	statusColor = (fError ? Color.red : Color.green);
      jProgressBar.setForeground(statusColor);
    }	
}
