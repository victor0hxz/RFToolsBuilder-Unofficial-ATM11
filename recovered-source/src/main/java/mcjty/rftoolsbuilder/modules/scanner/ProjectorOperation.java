package mcjty.rftoolsbuilder.modules.scanner;

public class ProjectorOperation {
   private ProjectorOpcode opcodeOn = ProjectorOpcode.NONE;
   private Double valueOn;
   private ProjectorOpcode opcodeOff = ProjectorOpcode.NONE;
   private Double valueOff;

   public ProjectorOpcode getOpcodeOn() {
      return this.opcodeOn;
   }

   public void setOpcodeOn(ProjectorOpcode opcodeOn) {
      this.opcodeOn = opcodeOn;
   }

   public Double getValueOn() {
      return this.valueOn;
   }

   public void setValueOn(Double valueOn) {
      this.valueOn = valueOn;
   }

   public ProjectorOpcode getOpcodeOff() {
      return this.opcodeOff;
   }

   public void setOpcodeOff(ProjectorOpcode opcodeOff) {
      this.opcodeOff = opcodeOff;
   }

   public Double getValueOff() {
      return this.valueOff;
   }

   public void setValueOff(Double valueOff) {
      this.valueOff = valueOff;
   }
}
