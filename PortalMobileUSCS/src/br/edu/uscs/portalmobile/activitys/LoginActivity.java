package br.edu.uscs.portalmobile.activitys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import br.edu.uscs.portalmobile.R;
import br.edu.uscs.portalmobile.libs.DetectaConexao;
import br.edu.uscs.portalmobile.libs.Mensagem;
import br.edu.uscs.portalmobile.libs.UserFunctions;
import br.edu.uscs.portalmobile.libs.ValidaCampo;

//Primeira Classe chamado quando aplicativo � aberto.
public class LoginActivity extends Activity {

	// Classe para controlar processo de Logar em Background.
	private class LoadViewTask extends AsyncTask<List<NameValuePair>, Integer, JSONObject> {

		InputStream is = null;
		JSONObject jObj = null;
		String json = "";

		// Obter dados do servidor em Background.
		@Override
		protected JSONObject doInBackground(List<NameValuePair>... parametros) {

			Log.i("importa_json", "Definindo parametros para enviar na requisi��o HTTP.");
			// Define lista de parametros na requisi��o HTTP
			HttpPost httpPost = new HttpPost(loginURL);
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(parametros[0]));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			Log.i("importa_json", "Defini��o de parametros concluida.");
			Log.i("importa_json", "Efetuando requisi��o HTTP.");
			// Executa requisi��o HTTP
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse httpResponse;
				httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("importa_json", "Requisi��o HTTP executada.");
			Log.i("importa_json", "Efetuando leitura HTTP.");
			// Efetua leitura da resposta HTTP
			try {
				// Causando lentid�o pelo parametro ..., 8) >>>
				// BufferedReader reader = new BufferedReader(new
				// InputStreamReader(is, "iso-8859-1"), 8);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				is.close();
				json = sb.toString();
				Log.i("JSON", json);
			} catch (Exception e) {
				Log.e("Buffer Error", "Error converting result " + e.toString());
			}
			Log.i("importa_json", "Leitura HTTP conclu�da.");
			Log.i("importa_json", "Interpretando JSON lido do servidor.");
			// Tentar interpretar JSON obtido do servidor
			try {
				jObj = new JSONObject(json);
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
			Log.i("importa_json", "Interpreta��o conclu�da.");
			// Retorna JSON ao onPostExecute();
			return jObj;
		}

		@Override
		protected void onPostExecute(JSONObject json) {

			try {
				// Caso obter dados v�lidos do servidor(sucess), efetuar logon.
				if (json.getString("success") != null) {
					String resposta = json.getString("success");
					if (Integer.parseInt(resposta) == 1) {

						Log.i("importa_json", "Efetuando logon com os dados obtidos do servido.");
						// Efetua processo de logon com os dados obtidos do
						// servidor.
						UserFunctions.fazerLogon(getApplicationContext(), json);

						// Exibe Menu do aplicativo ao estudante
						Intent dashboard = new Intent(LoginActivity.this, MenuActivity.class);
						dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(dashboard);
						finish();

					} else {
						Mensagem.erro(LoginActivity.this, "Ops!", "Matr�cula ou Senha incorreta.");
					}
				} else {
					Mensagem.erro(LoginActivity.this, "Ops!", "Matr�cula ou Senha incorreta.");
				}
			} catch (JSONException e) {

				/*
				 * usar este c�digo para debugar aplica��o.(Uso dos
				 * desenvolvedores). Mensagem.erro(LoginActivity.this, "Ops!",
				 * "JSONException - Erro 007 " + e.toString());
				 */

				// Mensagem de falha mais amigavel ao estudante.
				Mensagem.erro(LoginActivity.this, "Ops!", "Falha no servidor de dados.");
			}
			super.onPostExecute(json);
			Mensagem.conectandoDismiss();
		}

		// Antes de obter os dados do servidor.
		@Override
		protected void onPreExecute() {

			// Mostrar janela
			Mensagem.conectando(LoginActivity.this, "Conectando", "Por favor aguarde...", LoadViewTask.this, 30);
			super.onPreExecute();
			Log.i("importa_json", "Come�ou o processo de importa��o json.");

		}
	}

	/*
	 * Endere�o que a aplica��o ir� buscar todas as informa��es do estudante.
	 * 
	 * Aviso: Para efetuar teste local, o Android n�o entende 127.0.0.1, para
	 * isto deve ser usado o ip 10.0.2.2
	 * 
	 * O retorno desta URL Formato JSON.
	 */
	// private static String loginURL =
	// "http://10.0.2.2/PortalMobileUSCS/public_html/portal_mobule_uscs.php";
	private static String loginURL = "http://mobule.com.br/portal_mobule_uscs.php";

	private static String TAG_LOGIN = "login";
	Button btnLinkRecuperarSenha;

	Button btnLogin;

	EditText txtMatricula;

	EditText txtPassword;

	// Primeiro m�todo executado do aplicativo.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Verifica se est� logado.
		if (UserFunctions.estaLogado(getApplicationContext())) {

			// Inicia tela de apresenta��o do aplicativo(splashScreen)
			Intent splashScreen = new Intent(getApplicationContext(), SplashScreen.class);
			splashScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(splashScreen);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			finish();

		} else {

			// Apresenta tela de login para o estudante.
			setContentView(R.layout.login_layout);

			// Refer�ncia dos campos que estudante digita a matr�cula e senha.
			txtMatricula = (EditText) findViewById(R.id.loginMatricula);
			txtPassword = (EditText) findViewById(R.id.loginPassword);

			/*
			 * Refer�ncia dos bot�es Logar e Login.
			 * 
			 * Bot�o Logar � usado para mostrar os campos matricula e senha
			 * (anima��o).
			 * 
			 * Bot�o Login � usado para conectar estudando � aplicaca��o.
			 */
			btnLogin = (Button) findViewById(R.id.btnLogin);

			// Refer�ncia do bot�o "Recuperar Senha"
			btnLinkRecuperarSenha = (Button) findViewById(R.id.btnLinkRecuperarSenha);

			// Tratamento de conex�o para quando bot�o Login for pressionado.
			btnLogin.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {

					// Valida campo Matricula
					if (ValidaCampo.validaNulo(txtMatricula, "Campo obrigat�rio.") && ValidaCampo.validaNulo(txtPassword, "Campo obrigat�rio.")) {

						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(txtMatricula.getWindowToken(), 0);

						// Obt�m dados digitados pelo estudante para validar no
						// servidor de dados
						String matricula = txtMatricula.getText().toString();
						String password = txtPassword.getText().toString();

						// Prepara a lista de parametros para enviar ao
						// servidor.(Matricula e senha)
						List<NameValuePair> parametros = new ArrayList<NameValuePair>();
						parametros.add(new BasicNameValuePair("tag", TAG_LOGIN));
						parametros.add(new BasicNameValuePair("matricula", matricula));
						parametros.add(new BasicNameValuePair("password", password));

						// Verifica se tem conex�o para se conectar ao servidor
						// de dados.
						if (DetectaConexao.estaConectado(getApplicationContext())) {

							// Possui conex�o e come�a o processo para obter
							// dados ( Logar ).
							new LoadViewTask().execute(parametros);

						} else {

							// Sem conex�o, exibe alerta ao estudante
							Mensagem.alerta(LoginActivity.this, "Ops!", "Sem conex�o.");

						}
					}
				}

			});

			btnLinkRecuperarSenha.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					/*
					 * Jo�o Implementou(C�digo est� com ele).
					 */
				}
			});
		}
	}

}
