class Subsekvens {

	public final String SUBSEKVENS;
	private int antall = 1;

	Subsekvens (String s, int antall) {SUBSEKVENS = s;}

	public String hentSubNavn() {return SUBSEKVENS;}
	
	public int antallFrkmster () {return antall;}
	
	public void leggTilAnt(int ant) {antall += ant;}
	public void minusAnt(int ant) {antall -= ant;}
		
	public String toString() {
		return "(" + SUBSEKVENS + "," + antall + ")";
	}
}
