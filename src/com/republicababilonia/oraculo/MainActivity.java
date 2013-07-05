package com.republicababilonia.oraculo;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.republicababilonia.oraculo.util.APIUtil;

public class MainActivity extends Activity {
	private executaPergunta exec = null;
	static boolean taskLigada = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
				   if (actionId == EditorInfo.IME_NULL  
				      && event.getAction() == KeyEvent.KEYCODE_ENTER) { 
				      responder();
				   }
				   return true;
				}
		};
		
		final EditText editText = (EditText)findViewById(R.id.input);
		
			
		editText.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		        if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
		        	
		        	if(!taskLigada) {
		        		responder();
		        	}
		          return true;
		        }
		        return false;
		    }
		});
		Button perguntar = (Button)findViewById(R.id.botao);
		perguntar.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				responder();
			}
		});
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		
		final SpannableString s = new SpannableString("      http://itagaki.me");
		Linkify.addLinks(s, Linkify.WEB_URLS);
		final TextView message = new TextView(MainActivity.this);
		message.setText(s);
		message.setTextSize(30f);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		
		new AlertDialog.Builder(MainActivity.this)
		   .setCancelable(true)
		   .setIcon(android.R.drawable.ic_dialog_info)
		   .setPositiveButton("Ok", null)
		   .setView(message)
		   .show();
		return true;
		
		
	   }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void responder() {
		
		if(!isOnline()) {
			String msg = "Você precisa de conexão com a internet para se comunicar com o Oráculo";
    	    new AlertDialog.Builder(MainActivity.this)
 	       .setMessage(msg)
 	       .setNegativeButton("Ok", null)
 	       .show();
    	    return;
			
		}
		EditText input = (EditText)findViewById(R.id.input);
		String pergunta = input.getText().toString();
		pergunta = pergunta.replaceAll(" ", "%20");
		exec = new executaPergunta();
		exec.execute(pergunta);
		
		

	}		
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	@Override
    public void onBackPressed()
    {
		finish();
    }
    
	public void processResposta(String result) {
		try {
			
			JSONObject resposta = new JSONObject(result);
			
			if(resposta.getString("type").equals("url")) {
				String url = resposta.getString("msg");
				
				Intent intent = new Intent(this, WebViewActivity.class);
				Bundle b = new Bundle();
				b.putString("url", url);
				intent.putExtras(b);
				startActivity(intent);
				
			} else {
				String message = resposta.getString("msg");
			    new AlertDialog.Builder(MainActivity.this)
			       .setMessage(message)
			       .setNegativeButton("Obrigado Oráculo", null)
			       .show();
			}
			
			
			
		} catch (Exception e) {
			String msg = "Essa resposta é só para os merecedores.";
		    new AlertDialog.Builder(MainActivity.this)
		       .setMessage(msg)
		       .setNegativeButton("Obrigado Oráculo", null)
		       .show();
		}



	}
	
//	public String verificaEasterEgg(String pergunta) {
//		String resposta = null;
//		if(pergunta != null && pergunta.length() > 2) {
//			pergunta = pergunta.toLowerCase();
//			if(pergunta.contains("matraca")) {
//				resposta = "Joãozinho, aquele nazista metido a ariano? É um virjão!";
//			} else if(pergunta.contains("comelao") || pergunta.contains("comelão")) {
//				resposta = "Lindinho, bravo, suborbordinado da atética da comp, dizem que é chorão, mas quem chora sou eu longe de você meu comelão!";
//			} else if(pergunta.contains("maradona")) {
//				resposta = "Aquele mercenário? To esperando até hoje ele liberar o busão da festa do contrário!";
//			} else if(pergunta.contains("clit")) {
//				resposta = "Jesus Clits, aquele gordo safado? Só da migué na piscina!";
//			} else if(pergunta.contains("xavier")) {
//				resposta = "Dirige mal pra cacete!";
//			} else if(pergunta.contains("bo") && pergunta.contains("vizinho")) {
//				resposta = getResources().getString(R.string.firmezaBO);
//			}
//			
//			
//		}
//		return resposta;
//	}
	
	
class executaPergunta extends AsyncTask<String, Integer, String> {
		public ProgressDialog loading;
		@Override
		protected String doInBackground(String... pergunta) {
			try {
				
				if(pergunta[0].length() < 2) {
					pergunta[0] = "origem%20universo";
				}
				if(pergunta[0].indexOf("\n") > 0) {
					pergunta[0] = pergunta[0].substring(0, pergunta[0].indexOf("\n"));
				}
				pergunta[0] = pergunta[0].replaceAll("%20de%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("%20da%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("%20do%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("%20a%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("%20e%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("%20ou%20", "%20");
				pergunta[0] = pergunta[0].replaceAll("'", "");
			
				JSONObject json = APIUtil.getJSONFromUrl(APIUtil.WEBSERVICE+"/api/oraculo?q="+pergunta[0]);
				return json.toString();
						
//				
//				String urlYahoo = json
//						.getJSONObject("d")
//						.getJSONArray("results")
//						.getJSONObject(0)
//						.getString("Url");	
				
					
				
//	CODIGO ANTIGO
//				//verifica os easter eggs
//				String eggs = null;
//				
//				eggs = verificaEasterEgg(pergunta[0]);
//				if(eggs != null) {
//					Thread.sleep(3000);
//					return eggs;
//				}
//				
//				if(pergunta[0].length() < 2) {
//					pergunta[0] = "origem%20universo";
//				}
//				if(pergunta[0].indexOf("\n") > 0) {
//					pergunta[0] = pergunta[0].substring(0, pergunta[0].indexOf("\n"));
//				}
//				pergunta[0] = pergunta[0].replaceAll("%20de%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("%20da%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("%20do%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("%20a%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("%20e%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("%20ou%20", "%20");
//				pergunta[0] = pergunta[0].replaceAll("'", "");
//				
//				
//				pergunta[0] = "'"+pergunta[0]+"%20site:answers.yahoo.com'";
//				String url = "https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Web?$format=json&$top=8&Query="+pergunta[0];
//				JSONObject json = APIUtil.getJSONFromUrlBing(url);
//				
//				String urlYahoo = json
//						.getJSONObject("d")
//						.getJSONArray("results")
//						.getJSONObject(0)
//						.getString("Url");
//				
///*				String urlYahoo = json
//						.getJSONArray("items")
//						.getJSONObject(0)
//						.getString("link");*/
//				
//				String idYahoo = urlYahoo.substring(urlYahoo.indexOf("qid=")+4, urlYahoo.length());
//				
//				String queryYahoo = "select * from answers.getquestion where question_id=" + idYahoo;
//				
//				/*url = new URL("http://query.yahooapis.com/v1/public/yql?format=json&q=" + queryYahoo);
//				in = url.openStream();
//				r = new BufferedReader(new InputStreamReader(in));
//				total = new StringBuilder();
//				while ((line = r.readLine()) != null) {
//				    total.append(line);
//				}
//				r.close();
//				in.close();*/
//				url = "http://query.yahooapis.com/v1/public/yql?format=json&q=" + queryYahoo;
//				url = url.replaceAll(" ", "%20");
//				json = APIUtil.getJSONFromUrl(url);
//				
//				String resposta = json
//						.getJSONObject("query")
//						.getJSONObject("results")
//						.getJSONObject("Question")
//						.getString("ChosenAnswer");
//				
//
//				
//				return resposta;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    	return "null";
	    	
	    	

	    }
		
		@Override
		protected void onPreExecute() {
			taskLigada = true;
			loading = ProgressDialog.show(MainActivity.this, "", "Estou consultando os búzios...", 
					true, true, new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					executaPergunta.this.cancel(true);
					taskLigada = false;
				}
			});
		}
		
	 
	    protected void onPostExecute(String result) {
	    	taskLigada = false;
	    	loading.dismiss();
	    	processResposta(result);
	    }

	    
	}

}
	







