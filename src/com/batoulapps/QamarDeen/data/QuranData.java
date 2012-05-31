package com.batoulapps.QamarDeen.data;

public class QuranData {
   private int mStartAyah;
   private int mStartSura;
   private int mEndAyah;
   private int mEndSura;
   
   public QuranData(){
      this(0, 1, 0, 1);
   }
   
   public QuranData(int startAyah, int startSura, int endAyah, int endSura){
      this.mStartAyah = startAyah;
      this.mStartSura = startSura;
      this.mEndAyah = endAyah;
      this.mEndSura = endSura;
   }

   public int getStartAyah() {
      return mStartAyah;
   }

   public void setStartAyah(int startAyah) {
      this.mStartAyah = startAyah;
   }

   public int getStartSura() {
      return mStartSura;
   }

   public void setStartSura(int startSura) {
      this.mStartSura = startSura;
   }

   public int getEndAyah() {
      return mEndAyah;
   }

   public void setEndAyah(int endAyah) {
      this.mEndAyah = endAyah;
   }

   public int getEndSura() {
      return mEndSura;
   }

   public void setEndSura(int endSura) {
      this.mEndSura = endSura;
   }
}
