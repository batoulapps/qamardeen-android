package com.batoulapps.QamarDeen.data;

public class QuranData {
  private int mStartAyah;
  private int mStartSura;
  private int mEndAyah;
  private int mEndSura;

  public QuranData() {
    this(0, 1, 0, 1);
  }

  public QuranData(int startAyah, int startSura, int endAyah, int endSura) {
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

  /**
   * get the number of ayahs read
   *
   * @return the number of ayahs read
   */
  public int getAyahCount() {
    if (mStartSura > mEndSura) {
      return 0;
    } else if ((mStartSura == mEndSura) &&
        (mStartAyah > mEndAyah)) {
      return 0;
    } else if (mStartSura == mEndSura) {
      return mEndAyah - mStartAyah;
    }

    int ayahs = QamarConstants.SURA_NUM_AYAHS[mStartSura - 1];
    ayahs = ayahs - mStartAyah;
    for (int i = mStartSura + 1; i < mEndSura; i++) {
      ayahs += QamarConstants.SURA_NUM_AYAHS[i - 1];
    }
    ayahs += mEndAyah;
    return ayahs;
  }
}
