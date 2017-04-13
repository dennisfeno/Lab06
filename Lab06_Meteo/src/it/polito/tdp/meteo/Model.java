package it.polito.tdp.meteo;

import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private final static String[] citta = {"Genova","Milano","Torino"} ;
	
	private MeteoDAO dao ;
	private List<Citta> dati ;
	private double best = 0; 
	private List<SimpleCity> bestS;
	
	public Model() {
		
		dao = new MeteoDAO();
		dati = new ArrayList<Citta>();
		//bestS = new ArrayList<SimpleCity>();
		
		for(String s : citta) {
			
			dati.add(new Citta(s)) ;
		}
		
	}

	public String getUmiditaMedia(int mese) {
		
		StringBuilder sb = new StringBuilder();
		
		for(String s : citta){
			
			sb.append(String.format("L'umidità media per la città di %s è: %3.3f. \n", s, dao.getAvgRilevamentiLocalitaMese(mese, s)));
			
		}
		//System.out.println(sb);
		return sb.toString() ;  
	}
	
	/**
	 * livello: numero di città visitate
	 * sol corrente: lista di città e relativo costo.
	 * una sol parziale è completa se visito tre città
	 * una sol completa è sempre valida, vorrei l'ottimo
	 * 
	 * filtro sul costo... posso aggiungere anche un giorno... 
	 * 
	 * int step potrebbe memorizzare lo stato della ricerca. 
	 */
	
	public String trovaSequenza(int mese) {
		
		//Inizializzo le città
		
		for(Citta c : dati){
			c.setCounter(0);
			c.setRilevamenti( dao.getAllRilevamentiLocalitaMese(mese, c.getNome())
					);
			
		}
		
		best = Double.MAX_VALUE ;
		
		List<SimpleCity> sol = new ArrayList<SimpleCity>() ;
		
		recursive(sol,0);
		
		if(bestS.size()>0)
			return bestS.toString(); 
		else
			return "Nessuna soluzione :(";
	}
	
	/**
	 * funzione ricorsiva - i giorni vanno da 0 a 14
	 */

	private void recursive(List<SimpleCity> sol, int step){
		
		// Sono arrivato al giorno 15
		if (step == (NUMERO_GIORNI_TOTALI -1) ){
			
			if(punteggioSoluzione(sol)<best && checkSuccessivi(sol)){
				best = punteggioSoluzione(sol);
				bestS = new ArrayList<SimpleCity>(sol);
				//System.out.println(bestS + " - "+ best);	
			}
			return ;
		}
		
		for(Citta c : dati){ //scorro tutte le città
		
		// genera tutte le soluzioni	
			
		sol.add(new SimpleCity(c.getNome() , c.getRilevamenti().get(step).getUmidita() )) ;
		c.increaseCounter() ;
		
		// filtro sulle soluzioni
		
		if(controllaParziale(sol))
			recursive(sol, step +1) ;
		
		// backtracking
		
		c.decreaseCounter() ;
		sol.remove(step) ; 
			
		}
		
	}
	
	private boolean checkSuccessivi(List<SimpleCity> sol) {

			int count = 0 ;
			int cMin = Integer.MAX_VALUE ;
			for(int i = 1 ; i < sol.size() ; i++) { // SOL.SIZE = NUMERO_GIORNI_TOTALI
				
				if(sol.get(i).getNome().compareTo(sol.get(i-1).getNome()) == 0 ) { // se è uguale al precedente
					count ++ ;
				}
				else {
					if (count<cMin)
						cMin = count ; 
					count = 0 ;
				}
				//System.out.println("i "+i+" - cmin "+cMin + " - "+count ) ;
			}
			
			if (count < cMin) {
				cMin = count ; 
			}
			
		//System.out.println("cmin"+cMin + " - "+count ) ;
		if (cMin < NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN)
			return false ;
		
		
		for(Citta c : dati){ // devo visitare almeno un giorno TUTTE le città
			
			if(c.getCounter()==0)
				return false ;
			
		}
		
		return true;
	}

	/**
	 * costo totale di una soluzione. 
	 */
	
	private Double punteggioSoluzione(List<SimpleCity> soluzioneCandidata) {
		
		double score = 0.0;
			
		for (SimpleCity sc : soluzioneCandidata){ // costo proporzionale all'umidità
			score+= sc.getCosto() ;
		}
		
		for (int i=1 ; i < soluzioneCandidata.size() ; i++){ // costo per cambio città
			
			if(soluzioneCandidata.get(i).getNome().compareTo(soluzioneCandidata.get(i-1).getNome()) != 0 ) {
				score+=COST ;
			}
		}
		
		return score;
	}

	private boolean controllaParziale(List<SimpleCity> parziale) {
		
		for(Citta c : dati){ //scorro tutte le città
			
			if(c.getCounter()>NUMERO_GIORNI_CITTA_MAX)
				return false ;
			
		}
		
		// controllo almeno tre giorni nella stessa città, consecutivi
		
		int size = parziale.size() ;
		
		if (size == NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN -1 ) {
			if(parziale.get(0).getNome().compareTo(parziale.get(1).getNome()) != 0 ) {
				return false ; 
			}
		}
		
//		qua non faccio il controllo sui successivi perché non so cosa metto dopo, 
//		potrei farlo su quelli già messi ma tanto lo faccio dopo
//		if (size > 2 )
//			if (!checkSuccessivi(parziale))
//				return false ;
//					
		return true;
	}

	public double getBest() {
		return best;
	}


}
