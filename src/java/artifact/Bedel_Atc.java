// CArtAgO artifact code for project moodle

package artifact;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import abs.Course;
import abs.User;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;

public class Bedel_Atc extends db_art {
	private Course course;
	ArrayList<String> id_alunos_mensagem = new ArrayList<String>();
	ArrayList<String> lista_alunos_curso = new ArrayList<String>();
	
	@OPERATION
	void inc() {
		ObsProperty prop = getObsProperty("count");
		prop.updateValue(prop.intValue() + 1);
		signal("tick");
	}

	int idCourse = 0;
	String courseName = " ";
	String contextid;
	ArrayList<String> lista_tarefas_avaliadas;

	@OPERATION
	public void setIDcourse(int id) {
		System.out.println("Entrei em setIDcourse 1");
		idCourse = id;
		try {
			ResultSet rs = super.sfw("fullname", "mdl_course", "id=" + id);
			rs.next();
			course = new Course(id);
			courseName = course.getCourseName();
			rs = super.sfw("*", "mdl_context", "contextlevel = '50' and instanceid =" + idCourse);
			rs.next();
			contextid = rs.getString("id");
		} catch (SQLException e) {
		} catch (ClassNotFoundException e) {
		}
	}

	@OPERATION
	public ArrayList<String> lista_id_alunos_curso() throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em lista_id_alunos_curso 2");
		//ArrayList<String> lista_alunos_curso = new ArrayList<String>();
		//this.conexaoBD("lista_id_alunos_curso");
		String string;
		string = "SELECT id_aluno FROM mdl_tutor_tutor_aluno WHERE id_curso = " + this.idCourse;
		ResultSet rs = this.select(string);
		while (rs.next()) {
			String id = rs.getString("id_aluno");
			lista_alunos_curso.add(id);
		}
		//this.fecharConexao();
		return lista_alunos_curso;
	}


	@OPERATION
	public void printar(String msg, int X) {
		System.out.println("Entrei em printar 4");
		for (; X > 0; X--)
			System.out.println("msg = "+msg + ", para o curso "+ idCourse);
	}

	@OPERATION
	public void show_Students(OpFeedbackParam<String> students) {
		System.out.println("Entrei em showStudents 5");
		String stu = "";
		try {
			try {
				stu += "Alunos do curso: " + courseName + "\n";
				for (String s : this.lista_id_alunos_curso()) {
					stu += s + "\n";
				}
				printar("", 5);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		students.set(stu);
	}

	@OPERATION
	public void show_Teacher(OpFeedbackParam<String> teachers) {
		System.out.println("Entrei em showTeacher 6");
		ArrayList<String> professores = new ArrayList<String>();
		try {
			String string;
			String id = "";
			string = "SELECT id FROM mdl_role WHERE shortname='editingteacher'";
			// devolve o id do perfil professor
			ResultSet id_role_editingteacher = this.select(string);
			while (id_role_editingteacher.next()) {
				id = id_role_editingteacher.getString("id");
			}
			string = "SELECT * FROM mdl_role_assignments WHERE roleid = " + id;
			// devolve todos os professores
			ResultSet role_assignment_all_role_3 = this.select(string);
			string = "SELECT * FROM mdl_context WHERE contextlevel = '50' and instanceid ="	+ idCourse;
			// devolve dados da tabela context cujo contextlevel é 50 (contexto de curso)
			ResultSet context = this.select(string);
			while (role_assignment_all_role_3.next()) { // para todos os professores
				contextid = role_assignment_all_role_3.getString("contextid");
				while (context.next()) { // para todos os contextos
					String idcontext = context.getString("id");
					if (contextid.equals(idcontext)) { // se o contexto do professor é igual ao contexto
						int id_professor_curso = role_assignment_all_role_3.getInt("userid");
						professores.add("Nome: " + (new User(id_professor_curso)).getUserName() + "\t ID: " + id_professor_curso);
					}
				}
			}
		} catch (Exception e) {
		}
		String tea = "Professores do curso: " + courseName + "\n";
		for (String s : professores)
			tea += s + "\n";
		teachers.set(tea);
	}

	@OPERATION
	public void show_Me(OpFeedbackParam<String> string) {
		System.out.println("Entrei em showMe 7");
		string.set("Nome do curso: " + courseName + "\t\t ID do curso: " + idCourse);
	}

//**************COLOCAR FECHAMENTO DE OLHO APÓS CONFIGURAÇÃO DE GRAFO NO MOODLE *****************************
	@OPERATION
	boolean verifica_olho_fechado() throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em verifica_olho_fechado 8");
	//	this.conexaoBD("verifica_olho_fechado");
		// verifica olho fechado dos recursos e atividades, se for inicial deixa
		// aberto, se não for fecha e avisa ao professor.
		boolean atualizou_olho_fechado = false;
		int update_course_modules = 0;
		int update_iniciais = 0;
		int update_modinfo = 0;
		String string;
		string = "SELECT rec_ativ_id FROM mdl_tutor_dependencia WHERE curso_id=" + idCourse + " AND pre_req_id = '0'";
		ResultSet pre_req_0 = this.select(string); // 1a atividade e 1o recurso do curso (disciplina)
		ArrayList<String> iniciais = new ArrayList<String>();
		while (pre_req_0.next()) {
			iniciais.add(pre_req_0.getString("rec_ativ_id"));
			// lista com 1a atividade e 1 recurso
			string = "UPDATE mdl_course_modules SET visible = '1' WHERE id=" + pre_req_0.getString("rec_ativ_id");
			this.conexaoBD("verifica_olho_fechado");
			update_iniciais = this.update(string);
			this.fecharConexao();
			// System.out.println("iniciais " + update_iniciais);
		}
		string = "SELECT * FROM mdl_course_modules WHERE course = " + idCourse + " AND visible = '1' AND id !=" + iniciais.get(0) + " AND id !=" + iniciais.get(1);
		ResultSet course_modules = this.select(string);
		ArrayList<String> course_modules_list = new ArrayList<String>();
		while (course_modules.next()) {
			course_modules_list.add(course_modules.getString("id"));
			// lista de course_modules com olho aberto
			string = "UPDATE mdl_course_modules SET visible = '0' WHERE id=" + course_modules.getString("id");
			this.conexaoBD("verifica_olho_fechado");
			update_course_modules = this.update(string);
			this.fecharConexao();
			// System.out.println("course " + update_course_modules);
		}
		if (update_course_modules == 1 && update_iniciais == 1) {
			//string = "UPDATE mdl_course SET modinfo = '' WHERE id=" + idCourse;			
			update_modinfo = 1;//this.update(string);
			// System.out.println("modinfo" + update_modinfo);
		}
		if (update_modinfo == 1) {
			atualizou_olho_fechado = true;
		}
		//this.fecharConexao();
		return atualizou_olho_fechado;
		// volta true se modificou visibilidade.
	}

	@OPERATION /*ESTE MÉTODO TEM UMA VERSÃO NO TUTOR_ATC*/
	String converte_id_grade_item_em_id_module(String gradeItem) throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em converte_id_grade_item_em_id_module 10");
		ArrayList<Associacao> lista_modules = this.lista_modules();
	//	this.conexaoBD("converte_id_grade_item_em_id_module");
		String string;
		string = "SELECT * FROM mdl_grade_items WHERE courseid=" + idCourse + " AND id=" + gradeItem;
		ResultSet tabela_item_instance = this.select(string);
		
		String id_module = "";
		String item_instance = "";
		String item_module = "";
		String id_item_module = "";
		while (tabela_item_instance.next()) {
			item_instance = tabela_item_instance.getString("iteminstance");
			item_module = tabela_item_instance.getString("itemmodule");
		}
		for (int i = 0; i < lista_modules.size(); i++) {
			if (lista_modules.get(i).getItemmodule().equals(item_module)) {
				id_item_module = lista_modules.get(i).getId();
			}
		}
		string = "SELECT id FROM mdl_course_modules WHERE course=" + idCourse + " AND module=" + id_item_module + " AND instance=" + item_instance;
		ResultSet id_course_modules = this.select(string);
		while (id_course_modules.next()) {
			id_module = id_course_modules.getString("id");
		}
		return id_module;
	}
	
	@OPERATION
	Associacao converte_id_module_em_id_do_recurso_ativ(String id_module) throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em converte_id_module_em_id_do_recurso_ativ 11");
		Associacao dados_rec_at = new Associacao();
	//	this.conexaoBD("converte_id_module_em_id_do_recurso_ativ");
		String string;
		string = "SELECT * FROM mdl_course_modules WHERE course=" + idCourse + " AND id=" + id_module;
		ResultSet rs_tabela_course_modules = this.select(string);	
		rs_tabela_course_modules.next();
		dados_rec_at.setId(rs_tabela_course_modules.getString("module")); //id do tipo de modulo - quiz, assign, etc -  (tabela modules)
		dados_rec_at.setInstance(rs_tabela_course_modules.getString("instance"));
		string = "SELECT name FROM mdl_modules WHERE id=" + dados_rec_at.getId(); 
		ResultSet name = this.select(string);
		name.next();
		dados_rec_at.setItemmodule(name.getString("name"));
		return dados_rec_at;
	}

	@OPERATION
	void verifica_avaliacao_aluno() throws SQLException, ClassNotFoundException {
	System.out.println("Entrei em verifica_avaliacao_aluno 12");
	//verifica se algum aluno foi avaliado em alguma tarefa, se sim, atualiza para ele a visualização das seguintes atividades
	ArrayList<String> lista_id_gr_it_tarefas_com_avaliacao = new ArrayList<String>();
	lista_id_gr_it_tarefas_com_avaliacao = this.verifica_avaliacao_tarefa(); //lista com id de tarefas que já foram avaliadas (ou seja que o aluno já respondeu).
	//ArrayList<String> lista_alunos_curso = new ArrayList<String>();
	//lista_alunos_curso = this.lista_id_alunos_curso();
	String id_perfil = "";
	String id_aluno = "";
	double nota = 0;
	String string;
	String id_grade_item = " ";
	ArrayList<Associacao_notas_alunos_tarefa> lista_alunos_notas_tarefas = new ArrayList<Associacao_notas_alunos_tarefa>();
	//this.conexaoBD("verifica_avaliacao_aluno");
	for(int i = 0; i < lista_id_gr_it_tarefas_com_avaliacao.size(); i ++){// para todas as tarefas que já foram avaliadas no curso
		ArrayList<String> id_alunos_avaliados = new ArrayList<String>();
		id_grade_item = lista_id_gr_it_tarefas_com_avaliacao.get(i); //pega id_grade_item
		lista_alunos_notas_tarefas = this.devolve_lista_alunos_notas(id_grade_item); //da tabela grade grades devolve id_aluno e nota em cada item da lista
		// System.out.println("id_grade_item = "+ id_grade_item+" tem "+lista_alunos_notas_tarefas.size()+" alunos avaliados");
		//lista com id_aluno e nota de cada tarefa avaliada
		string = "SELECT * FROM mdl_tutor_alunos_avaliados WHERE id_curso = "+ idCourse +" AND id_grade_item = " + id_grade_item;
		ResultSet rs_tabela_alunos_avaliados_do_id_grade_item = this.select(string); 
		//para cada id_grade_item retorna tabela com dados dos alunos que já tiveram o cálculo do perfil feito após ter a nota nesse id_grade_item
		while(rs_tabela_alunos_avaliados_do_id_grade_item.next()){
			if (!rs_tabela_alunos_avaliados_do_id_grade_item.getString("nro_calculo").equals("0")){
				id_alunos_avaliados.add(rs_tabela_alunos_avaliados_do_id_grade_item.getString("id_aluno"));	
				//System.out.println("id alunos avaliados = "+ rs_tabela_alunos_avaliados_do_id_grade_item.getString("id_aluno"));
			}
		}
		for (int j = 0; j < lista_alunos_notas_tarefas.size(); j++){
			id_aluno = lista_alunos_notas_tarefas.get(j).getId_aluno();
			nota = lista_alunos_notas_tarefas.get(j).getNota();
			if(!id_alunos_avaliados.contains(id_aluno)){
				if (!id_alunos_mensagem.contains(id_aluno)){
					System.out.println("adicionou id do aluno "+id_aluno+" na lista id_alunos_mensagem");
					id_alunos_mensagem.add(id_aluno);
				}
			//se entra aqui quer dizer que o aluno ainda não tinha sido avaliado nessa tarefa e por isso não há um registro na tabela tutor alunos avaliados, com o id_grade_item dessa iteração do for.
			// Nesse caso, como o aluno tem uma nota no grade grades, ele fez a atividade e foi avaliado (no quiz) e precisa ser calculada a nota perfil dele
			// Além disso, precisa disponibilizar os próximos recursos para ele e atualizar a tabela alunos avaliados.
				id_perfil = this.calcula_nota_perfil_aluno(id_aluno, id_grade_item, nota);
			if (!id_perfil.equals("0")){
				this.disponibiliza_rec_at_aluno(id_aluno, id_grade_item, id_perfil);
			}else{
				//Se entra aqui id_perfil é 0, o aluno teve uma nota menor que 25 e não pode continuar no curso. Abrir nova tentativa para fazer atividade ==> id_grade_item
				System.out.println("Entrou em abre nova tentativa");
				this.disponibiliza_rec_at_aluno_perfil0(id_aluno, id_grade_item, id_perfil);
				this.abre_nova_tentativa(id_aluno, id_grade_item);
			}
			}
		}	
		System.out.println("tamanho lista id_alunos_avaliados = "+ id_alunos_avaliados.size());
	}
	//this.fecharConexao();
	}
	
	@OPERATION
	void pega_Id_User(OpFeedbackParam<String> id, OpFeedbackParam<String> id_curso, OpFeedbackParam<Boolean> bool) throws SQLException, ClassNotFoundException {
		String id_aluno = "";
		bool.set(true);
		if (id_alunos_mensagem.size() > 0){
			id_aluno = id_alunos_mensagem.remove(0);
			id.set(id_aluno);
			id_curso.set(Integer.toString(idCourse));
		}else{
			bool.set(false);
		}
	}
	
		@OPERATION
	void purge_course_cache(String id_curso) throws SQLException, ClassNotFoundException {
		System.out.println("entrei em purge course cache");
		String string;
		long datahoje = System.currentTimeMillis() / 1000;
		string = "UPDATE mdl_course SET cacherev = "+ datahoje +" WHERE id="  + id_curso;
		System.out.println("UPDATE mdl_course SET cacherev = "+ datahoje +" WHERE id="  + id_curso);
		this.conexaoBD("purge_course_cache");
		this.update(string);
		this.fecharConexao();
	}
	
	
	@OPERATION
	void disponibiliza_rec_at_aluno_perfil0(String id_aluno, String id_grade_item, String id_perfil) throws SQLException, ClassNotFoundException {
	//disponibiliza as atividades de reforço para os alunos que repetiram a partir de 3 vezes uma atividade e tiveram como nota um valor menor que 2,5
	System.out.println("Entrei em disponibiliza_rec_at_aluno_perfil0 NOVO");
	String string;
	String id_module = this.converte_id_grade_item_em_id_module(id_grade_item);		
	String adicional = "";
			//	this.conexaoBD("disponibiliza_rec_at_aluno_perfil0");
				ArrayList<String> lista_modules_pre_req = new ArrayList<String>();
				lista_modules_pre_req = this.retorna_pre_requisitos_do_modulo(id_module);
				System.out.println(" tamanho lista modules pre req = "+ lista_modules_pre_req.size());
				for (int n = 0; n < lista_modules_pre_req.size(); n++){
					String id_modules_pre_req = lista_modules_pre_req.get(n);
					System.out.println("lista_modules_pre_req "+ n + " = "+ id_modules_pre_req);
					string = "SELECT * FROM mdl_course_modules WHERE id = "+ id_modules_pre_req + " AND visible = '0'";
					ResultSet rs_modules_ocultos = this.select(string);
					if (rs_modules_ocultos.isBeforeFirst()){
						//se existe pelo menos um valor no resultado ou seja, o módulo não está visível
						rs_modules_ocultos.next();
						//para criar os grupos básicos adicionais do conteúdo do módulo I
						System.out.println("id modules ocultos que tem visible 0 = "+ rs_modules_ocultos.getString("id"));
						if (rs_modules_ocultos.getString("id").equals(id_modules_pre_req)){
							System.out.println("module estava oculto antes de entrar nesse if");
							adicional = "adicional";
							int id_grupo_adicional = this.cria_grupo_module(lista_modules_pre_req.get(n), "1", adicional);
							this.insere_aluno_grupo(id_aluno, id_grupo_adicional);
							String strbasicoadicional =  "'{\"op\":\"&\",\"c\":[{\"type\":\"group\",\"id\":"+ id_grupo_adicional +"}],\"showc\":[false]}'";
							string = "UPDATE mdl_course_modules SET availability = "+ strbasicoadicional +", visible = '1', visibleold = '1' WHERE id="  + lista_modules_pre_req.get(n);
							this.conexaoBD("disponibiliza_rec_at_aluno_perfil0");
							this.update(string);
							this.fecharConexao();
						}
					}else{
						//quer dizer que o módulo está já está visible
						System.out.println("id do modulo = "+id_modules_pre_req+" que já existe com um grupo para o módulo e já está visível");
						string = "SELECT * FROM mdl_course_modules WHERE id = "+ id_modules_pre_req;
						ResultSet rs_modules_com_grupo = this.select(string);
						if (rs_modules_com_grupo.isBeforeFirst()){
						//se existe pelo menos um valor no resultado
							rs_modules_com_grupo.next();
							int id_grupo_no_modulo = this.getGroupId(rs_modules_com_grupo.getString("availability"));
							if (id_grupo_no_modulo != -1){
								//se existe o grupo 
								this.insere_aluno_grupo(id_aluno, id_grupo_no_modulo);						
							}
						}
					}
				}
				System.out.println("Saiiiiiii de disponibiliza_rec_at_aluno_perfil0 NOVO");
				//this.fecharConexao();
	}
	
	
	@OPERATION
	void disponibiliza_rec_at_aluno(String id_aluno, String id_grade_item, String id_perfil) throws SQLException, ClassNotFoundException {
	System.out.println("Entrei em disponibiliza_rec_at_aluno 13");
	String id_module = this.converte_id_grade_item_em_id_module(id_grade_item); //id grade item da atividade avaliada que foi gatilho para atualizar o perfil do aluno, convertida em id_module
	ArrayList<Associacao_id_module_perfil_id> lista_id_module_rec_at_para_mostrar = new ArrayList<Associacao_id_module_perfil_id>();
	//this.conexaoBD("disponibiliza_rec_at_aluno");
	String string;
	string = "SELECT rec_ativ_id FROM mdl_tutor_dependencia WHERE curso_id = "+idCourse+" and pre_req_id="+ id_module;
	// pega as atividades que têm como pre requisito a última atividade feita pelo alunos, para serem mostradas a seguir para o aluno
	ResultSet rec_at_ids = this.select(string);
	while (rec_at_ids.next()){
		if (!lista_id_module_rec_at_para_mostrar.contains(rec_at_ids.getString("rec_ativ_id"))){
			string = "SELECT perfil_id FROM mdl_tutor_rec_at_perfil WHERE curso_id = "+idCourse+" and rec_ativ_id="+ rec_at_ids.getString("rec_ativ_id");
			ResultSet perfil_id = this.select(string);
			perfil_id.next();
			Associacao_id_module_perfil_id id_mod_perfil_id = new Associacao_id_module_perfil_id();
			id_mod_perfil_id.setPerfilId(perfil_id.getString("perfil_id"));
			id_mod_perfil_id.setIdModule(rec_at_ids.getString("rec_ativ_id"));				
			lista_id_module_rec_at_para_mostrar.add(id_mod_perfil_id);
			//lista com o id_module e perfil (1, 2, 3 ou 4) da atividade que deve ser mostrada para os alunos
		}
	}
	//this.fecharConexao();			
	int id_grupo = 0;
	int id_grupo_geral = 0;
	Associacao id_rec_at; //id - itemmodule - instance
	//id -> "module" da tabela course_modules --> tipo de modulo - quiz, assign, etc (tabela modules)
	//instance --> "instance" da tabela course_modules
	//itemmodule --> "name" da tabela modules
	for (int i = 0; i < lista_id_module_rec_at_para_mostrar.size(); i++){
		String perfilId = lista_id_module_rec_at_para_mostrar.get(i).getPerfilId(); //perfil 1, 2, 3 ou 4 do modulo
		String idModule = lista_id_module_rec_at_para_mostrar.get(i).getIdModule(); //id_module
		System.out.println("Para mostrar: perfilId = "+ perfilId + " idModule = "+idModule);
		if (id_perfil.equals(perfilId)){ //se o recurso ou a atividade são do perfil que o aluno está no momento
				id_rec_at = this.converte_id_module_em_id_do_recurso_ativ(idModule);
				id_grupo = this.cria_grupo_module(idModule, perfilId, ""); //cria grupo ou verifica se o grupo já existe e devolve o id do grupo.
				this.insere_aluno_grupo(id_aluno, id_grupo);
				String tabela = "";
				String id_r_a = "";
				if (id_rec_at.getItemmodule().equals("quiz")){
					tabela = "quiz";
					id_r_a = id_rec_at.getInstance();
				}
				if (id_rec_at.getItemmodule().equals("assign")){ 
					tabela = "assign";
					id_r_a = id_rec_at.getInstance();
				}
			if (perfilId.equals("1")){
				String adicional = "";
		//		this.conexaoBD("disponibiliza_rec_at_aluno");
				ArrayList<String> lista_modules_pre_req = new ArrayList<String>();
				lista_modules_pre_req = this.retorna_pre_requisitos_do_modulo(id_module);
				for (int n = 0; n < lista_modules_pre_req.size(); n++){
					String id_modules_pre_req = lista_modules_pre_req.get(n);
					System.out.println("lista_modules_pre_req "+ n + " = "+ id_modules_pre_req);
					string = "SELECT * FROM mdl_course_modules WHERE id = "+ id_modules_pre_req + " AND visible = '0'";
					ResultSet rs_modules_ocultos = this.select(string);
					if (rs_modules_ocultos.isBeforeFirst()){ 
						//se existe pelo menos um valor no resultado
						rs_modules_ocultos.next();
						//para criar os grupos básicos adicionais do conteúdo do módulo I
						System.out.println("id modules ocultos que tem visible 0 = "+ rs_modules_ocultos.getString("id"));
						if (rs_modules_ocultos.getString("id").equals(id_modules_pre_req)){
							System.out.println("module estava oculto antes de entrar nesse if");
							adicional = "adicional";
							int id_grupo_adicional = this.cria_grupo_module(lista_modules_pre_req.get(n), perfilId, adicional);
							this.insere_aluno_grupo(id_aluno, id_grupo_adicional);
							String strbasicoadicional =  "'{\"op\":\"&\",\"c\":[{\"type\":\"group\",\"id\":"+ id_grupo_adicional +"}],\"showc\":[false]}'";
							string = "UPDATE mdl_course_modules SET availability = "+ strbasicoadicional +", visible = '1', visibleold = '1' WHERE id="  + lista_modules_pre_req.get(n);
							this.conexaoBD("disponibiliza_rec_at_aluno");
							this.update(string);
							this.fecharConexao();							
						}
					}
				}
				//this.fecharConexao();
			}
			if (perfilId.equals("1")||perfilId.equals("2")||perfilId.equals("3")){
				this.conexaoBD("disponibiliza_rec_at_aluno");
				String str_id_grupo =  "'{\"op\":\"&\",\"c\":[{\"type\":\"group\",\"id\":"+id_grupo+"}],\"showc\":[false]}'";
				string = "UPDATE mdl_course_modules SET availability = "+ str_id_grupo +", visible = '1', visibleold = '1' WHERE id=" + idModule;
				this.update(string);	
				//convertendo idmodule em id do recurso ou atividade para inserir a data de início e fim nela.
				this.fecharConexao();
			} 
		}
		System.out.println("antes do if com perfil 4");
		if (perfilId.equals("4")){
			System.out.println("perfil da tarefa é 4 ou seja fica disponível para os alunos com nota maior que 25");
			// CÓDIGO PARA ABRIR O OLHO.
				id_grupo_geral = this.cria_grupo_module(idModule, perfilId, ""); //cria grupo ou verifica se o grupo já existe e devolve o id do grupo (se o id é -2 quer dizer que já existe um grupo relacionado e criado nesse módulo).
				this.insere_aluno_grupo(id_aluno, id_grupo_geral);
				this.conexaoBD("disponibiliza_rec_at_aluno");
				String strgeral =  "'{\"op\":\"&\",\"c\":[{\"type\":\"group\",\"id\":"+id_grupo_geral+"}],\"showc\":[false]}'";
				string = "UPDATE mdl_course_modules SET availability = "+ strgeral +", visible = '1', visibleold = '1'  WHERE id="  + idModule;
				//string = "UPDATE mdl_course_modules SET visible = '1', visibleold = '1' WHERE id="  + idModule;
				this.update(string);
				this.fecharConexao();
		}		
	}
	}
	
	@OPERATION
	void abre_nova_tentativa(String id_aluno, String id_grade_item) throws SQLException, ClassNotFoundException {
		this.conexaoBD("abre_nova_tentativa");
		String string = "UPDATE mdl_grade_grades SET rawgrade = null, usermodified = '2 ', finalgrade = null, timecreated = null, timemodified = null, aggregationstatus = 'novalue', aggregationweight = '0' WHERE itemid="  + id_grade_item + " AND userid = "+ id_aluno;
		this.update(string);
		String idModule = this.converte_id_grade_item_em_id_module(id_grade_item);
		Associacao id_rec_at = this.converte_id_module_em_id_do_recurso_ativ(idModule);
		string = "DELETE FROM mdl_quiz_grades WHERE quiz="  + id_rec_at.getInstance() + " AND userid = "+ id_aluno;
		this.update(string);
		string = "DELETE FROM mdl_quiz_attempts WHERE quiz="  + id_rec_at.getInstance() + " AND userid = "+ id_aluno;
		this.update(string);
		this.fecharConexao();
	}
	

	
	@OPERATION /*ESTE MÉTODO TEM UMA VERSÃO NO TUTOR_ATC*/
	int cria_grupo_module(String idModule, String perfilId, String adicional) throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em cria_grupo_module 14");
		//cria grupo em um modulo com perfil específico // 1 = básico // 2 = médio // 3 = avançado // 4 = geral
		//this.conexaoBD("cria_grupo_module");
		String string;
		//ArrayList<String> lista_id_ultimos_grupos = new ArrayList<String>();
		int id_grupo_existente_no_modulo = 0;
		string = "SELECT availability FROM mdl_course_modules where id = "+ idModule; //pega campo availability do módulo para ver se ele já tem um grupo criado e relacionado a ele
		ResultSet rs_availability = this.select(string);
		rs_availability.next();
		if (rs_availability.getString("availability") != null){
			id_grupo_existente_no_modulo = this.getGroupId(rs_availability.getString("availability"));
			//o valor em availability é diferente de nulo, agente pega o id do grupo com o qual está relacionado esse grade item
		}else {
			//availability é null então, não tem id de grupo válido, fica como -1
			id_grupo_existente_no_modulo = -1;
		}
		if (id_grupo_existente_no_modulo != -1){
		//já existe um grupo relacionado e criado nesse módulo.
			return id_grupo_existente_no_modulo;
		}else if (id_grupo_existente_no_modulo == -1){
		//quer dizer não há nenhum grupo relacionado com esse módulo, então, cria o grupo.
		WebServiceMoodle ws = new WebServiceMoodle();
		try{
			if (perfilId.equals("1")){
				if (adicional.equals("adicional")){
					ws.cria_grupos("Adaptação adicional B " + idModule, idCourse);
				}else{
					ws.cria_grupos("Adaptação B " + idModule, idCourse);	
				}
			}if (perfilId.equals("2")){
				ws.cria_grupos("Adaptação M " + idModule, idCourse);
			}if (perfilId.equals("3")){
				ws.cria_grupos("Adaptação A " + idModule, idCourse);
			}if (perfilId.equals("4")){
				ws.cria_grupos("Adaptação G " + idModule, idCourse);
			}
			//this.conexaoBD("cria_grupo_module");
			string = "SELECT * FROM mdl_groups WHERE courseid = "+idCourse+" ORDER BY id DESC LIMIT 1";
			ResultSet rs_id_grupo = this.select(string);
			rs_id_grupo.next();
			id_grupo_existente_no_modulo = rs_id_grupo.getInt("id");
		}catch(Exception e){
				System.out.println("criei grupo WS dentro da exception "+ e.getMessage()+ " para o curso "+ idCourse);
		}			
		System.out.println("criei grupo NOVO " + id_grupo_existente_no_modulo + " para o curso "+ idCourse);
		}
		//this.fecharConexao();
		return id_grupo_existente_no_modulo;
	}
	
	@OPERATION /*ESTE MÉTODO TEM UMA VERSÃO NO TUTOR_ATC*/
	void insere_aluno_grupo(String id_aluno, int id_grupo) throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em insere_aluno_grupo 15");
		long datahoje = System.currentTimeMillis() / 1000;
		//this.conexaoBD("insere_aluno_grupo");
		String string;
		boolean esta_no_grupo = this.verifica_no_grupo(id_aluno, id_grupo);
		string = "INSERT INTO mdl_groups_members(groupid,userid,timeadded) VALUES (" + id_grupo + ", " + id_aluno + " , "+ datahoje +" )";
		this.conexaoBD("insere_aluno_grupo");
		this.update(string);
		System.out.println("Inseri aluno "+ id_aluno+ " no grupo "+ id_grupo);
		this.fecharConexao();
	}
	
	@OPERATION 
	boolean verifica_no_grupo(String id_aluno, int id_grupo) throws SQLException, ClassNotFoundException {
		String string;
		boolean esta = false;
		string = "SELECT id FROM mdl_groups_members WHERE groupid = "+ id_grupo+ " AND userid = "+ id_aluno;
		ResultSet rs_id_existe = this.select(string);
		if (rs_id_existe.isBeforeFirst()){
			System.out.println("aluno id = "+ id_aluno+ " está no grupo idgrupo = "+id_grupo);
			esta = true; 
		}
		System.out.println("Entrei em verifica_no_Grupo 00000000000 aluno "+ id_aluno+" já está no grupo" + id_grupo + " = " + esta);
		return esta;
	}
	
	@OPERATION
	String calcula_nota_perfil_aluno(String id_aluno, String id_grade_item, double nota) throws SQLException, ClassNotFoundException {
	System.out.println("Entrei em calcula_nota_perfil_aluno 16");
	//this.conexaoBD("calcula_nota_perfil_aluno");
	double soma_notas = 0;
	double media = 0;
	int quant_items = 0;
	String string;
	string = "SELECT * FROM mdl_tutor_alunos_avaliados where id_curso = " + idCourse + " AND id_aluno = " + id_aluno + " ORDER BY id DESC";
	ResultSet rs_aluno_em_alunos_avaliados = this.select(string);
	while (rs_aluno_em_alunos_avaliados.next()){
		if (!rs_aluno_em_alunos_avaliados.getString("nro_calculo").equals("0")){
			soma_notas = soma_notas + rs_aluno_em_alunos_avaliados.getDouble("nota");
			quant_items++;
		}		
	}
	ArrayList<Associacao_Aluno_Logs> lista_alunos_cont_log = this.verifica_log_pre_req(this.converte_id_grade_item_em_id_module(id_grade_item)); // lista com id do aluno e quant de logs nos pre_req 
	double notaComLog = 2;
	double notaSemLog = 4;
	double nota_log = notaSemLog;
	boolean temlog = false;
	for (int i = 0; i < lista_alunos_cont_log.size(); i++){
		if (lista_alunos_cont_log.get(i).getId_aluno().equals(id_aluno)){
			temlog = true;
			if (lista_alunos_cont_log.get(i).getCount_logs() > 0) {
				nota_log = notaComLog;
			} else {
				nota_log = notaSemLog;
			}
		}
		if (!temlog) {
			nota_log = notaSemLog;
		}
	}
	quant_items = quant_items + 1; // esse é o número de vezes que foi feito o cálculo de notas para o aluno (nro_calculo)
	nota = nota + nota_log;
	media = (soma_notas + nota)/(quant_items);
	String id_perfil = "0";
	if (nota <= 25){		
		if (id_grade_item.equals("182")){ /*ISTO NA ÚLTIMA ATIVIDADE (TESTE DE APROVEITAMENTO)*/
			id_perfil = "1";
		} else {
			id_perfil = "0";
			quant_items = 0;
		}
		string = "INSERT INTO mdl_tutor_alunos_avaliados(id_curso, id_aluno, id_perfil, id_grade_item, nota, media, nro_calculo) VALUES (" + idCourse + ", " + id_aluno + ", " + id_perfil + ", " + id_grade_item + ", " + nota + "," + media + ", " + quant_items + " )";
		this.conexaoBD("calcula_nota_perfil_aluno");
		this.update(string);
		this.fecharConexao();
		return id_perfil;
	}
	if (media > 25 && media <= 54){
		id_perfil = "1";
	} else if (media > 54 && media < 80){
		id_perfil = "2";
	} else if (media >= 80) {
		id_perfil = "3";
	}
	string = "INSERT INTO mdl_tutor_alunos_avaliados(id_curso, id_aluno, id_perfil, id_grade_item, nota, media, nro_calculo) VALUES (" + idCourse + ", " + id_aluno + ", " + id_perfil + ", " + id_grade_item + ", " + nota + "," + media + ", " + quant_items + " )";
	this.conexaoBD("calcula_nota_perfil_aluno");
	this.update(string);
	this.fecharConexao();
	return id_perfil;
}
	
	public ArrayList<Associacao_notas_alunos_tarefa> devolve_lista_alunos_notas(String id_gr_it)throws SQLException, ClassNotFoundException {
		//System.out.println("Entrei em devolve_lista_alunos_notas 17");
		//devolve uma lista com os alunos e as notas deles no id grade item do argumento, pegando da tabela grade grades
		String string;
		//this.conexaoBD("devolve_lista_alunos_notas");
		string = "SELECT * FROM mdl_grade_grades WHERE itemid = "+ id_gr_it;
		String nulo = "NULL";
		ResultSet userid_rawgrade = this.select(string);
		ArrayList<Associacao_notas_alunos_tarefa> lista_notas_alunos_avaliados = new ArrayList<Associacao_notas_alunos_tarefa>();
		while (userid_rawgrade.next()){
		//	System.out.println("itemid = "+ userid_rawgrade.getString("itemid"));
			if (!userid_rawgrade.getString("itemid").equals("192")){ /* VERIFICAR PARA MUDAR ISTO COM O ID DO QUESTIONARIO INICIAL*/
				if (userid_rawgrade.getString("rawgrade") != null){
					//System.out.println("id do usuário"+ userid_rawgrade.getString("userid") +"- rawgrade "+ userid_rawgrade.getString("rawgrade"));
					Associacao_notas_alunos_tarefa ass_nota_alunos = new Associacao_notas_alunos_tarefa();
					ass_nota_alunos.setId_aluno(userid_rawgrade.getString("userid"));
					ass_nota_alunos.setNota(Double.parseDouble(userid_rawgrade.getString("rawgrade")));
					lista_notas_alunos_avaliados.add(ass_nota_alunos);
				}
			}
		}	
		this.fecharConexao();
		return lista_notas_alunos_avaliados;	
	}
	
	@OPERATION
	ArrayList<String> verifica_avaliacao_tarefa() throws SQLException, ClassNotFoundException {
	System.out.println("Entrei em verifica_avaliacao_tarefa 18");
	//Verifica nas tabelas grade_items e grade_grades quais são as tarefas que já foram avaliadas (ou seja que o aluno já respondeu) e retorna uma lista com os id delas.
	ArrayList<String> lista_id_gr_it_curso = new ArrayList<String>();
	//this.conexaoBD("verifica_avaliacao_tarefa");
	String string;
	string = "SELECT id FROM mdl_grade_items where courseid = " + idCourse;
	ResultSet rs_id = this.select(string);
	while(rs_id.next()){
		lista_id_gr_it_curso.add(rs_id.getString("id"));
		//lista com todos os id_grade_items do curso
	}
	ArrayList<String> lista_id_grade_items_avaliados = new ArrayList<String>();
	for (int i = 0; i < lista_id_gr_it_curso.size(); i++){
		String id_gr_it = lista_id_gr_it_curso.get(i);
		string = "SELECT * FROM mdl_grade_grades where itemid = " + id_gr_it;
		ResultSet rs_items_curso = this.select(string);
		while(rs_items_curso.next()){
			if (rs_items_curso.getString("rawgrade") != null && !lista_id_grade_items_avaliados.contains(id_gr_it)){
				lista_id_grade_items_avaliados.add(id_gr_it);
				//System.out.println("item_avaliado = "+id_gr_it);
				//lista com todos os id_grade_items do curso que têm avaliação para algum aluno.
			}
		}
	}
	return lista_id_grade_items_avaliados;	
	}


	public ArrayList<Associacao> lista_modules() throws SQLException, ClassNotFoundException {/*ESTE MÉTODO TEM UMA VERSÃO NO TUTOR_ATC*/
		System.out.println("Entrei em lista_modules 19");
		//this.conexaoBD("lista_modules");
		String string;
		string = "SELECT * FROM mdl_modules"; // seleciona a tabela modules
		ResultSet modules = this.select(string);
		ArrayList<Associacao> id_modules = new ArrayList<Associacao>();
		while (modules.next()) {
			Associacao ass_modules = new Associacao();
			ass_modules.setId(modules.getString("id")); // id = id dos m�dulos
			ass_modules.setItemmodule(modules.getString("name")); // itemmodule = nome dos módulos
			id_modules.add(ass_modules); // lista com id e nome de modules
		}
		this.fecharConexao();
		return id_modules;
	}
	
	public static int getGroupId(String jsonFromDB) {
		System.out.println("Entrei em getGroupId 20");
		System.out.println("jsonFromDB"+jsonFromDB);
		//devolve o id do grupo que está informado na string do campo availability da tabela mdl_course_modules
		if (jsonFromDB != null){
			if (jsonFromDB.contains("\"type\":\"group\"")) {
				int idIndex = jsonFromDB.indexOf("\"id\":") + 5;
				int endIndex = jsonFromDB.indexOf("}");
				String stringId = jsonFromDB.substring(idIndex, endIndex);
				return Integer.parseInt(stringId);
			}
		}
		return -1;
	}

	@OPERATION
	ArrayList<Associacao_Aluno_Logs> verifica_log_pre_req(String idTarefa)
			throws SQLException, ClassNotFoundException {
		System.out.println("Entrei em verifica_log_pre_req 21");
	//	this.conexaoBD("verifica_log_pre_req");
		// Devolve um objeto associacao_aluno_logs, com id do aluno e a quant de logs nos recursos.
		String string;
		// idTarefa = "4";
		string = "SELECT * FROM mdl_tutor_dependencia WHERE curso_id=" + idCourse + " AND rec_ativ_id=" + idTarefa;
		ResultSet tutor_dependencia = this.select(string);
		ArrayList<Associacao_dependencia> lista_rec_ativ_id = new ArrayList<Associacao_dependencia>();
		while (tutor_dependencia.next()) {
			Associacao_dependencia ass_dep = new Associacao_dependencia();
			ass_dep.setRec_ativ_id(tutor_dependencia.getString("rec_ativ_id"));
			ass_dep.inserePreReq(tutor_dependencia.getString("pre_req_id"));
			boolean igual = false;
			for (int i = 0; i < lista_rec_ativ_id.size(); i++) {
				if (lista_rec_ativ_id.get(i).getRec_ativ_id().equals(tutor_dependencia.getString("rec_ativ_id"))) {
					igual = true;
					ArrayList<String> listaDependencia = new ArrayList<String>();
					listaDependencia = lista_rec_ativ_id.get(i).getPre_req_id();
					listaDependencia.add(tutor_dependencia.getString("pre_req_id"));
					// Adiciona pre_req da mesma rec_ativ na lista.
				}
			}
			if (!igual) {
				lista_rec_ativ_id.add(ass_dep);
				// Adiciona � lista um objeto associacao_dependencia, com id da rec_ativ e a lista de pre_req dela.
			}
		}
		ArrayList<String> recursos_pre = new ArrayList<String>();
		for (int j = 0; j < lista_rec_ativ_id.size(); j++) {
			for (int k = 0; k < lista_rec_ativ_id.get(j).getPre_req_id().size(); k++) {
				string = "SELECT id FROM mdl_course_modules WHERE id=" + lista_rec_ativ_id.get(j).getPre_req_id().get(k) + " AND (module=15 OR module=13)";
				ResultSet recursos = this.select(string);
				if (recursos.next()) {
					recursos_pre.add(recursos.getString("id"));
				}
			}
		}
		ArrayList<String> course_students = lista_alunos_curso;//this.lista_id_alunos_curso();
	//	this.conexaoBD("verifica_log_pre_req");
		ArrayList<Associacao_Aluno_Logs> alunos_cont_log = new ArrayList<Associacao_Aluno_Logs>();
		for (int l = 0; l < course_students.size(); l++) {
			for (int m = 0; m < recursos_pre.size(); m++) {
				int count_log = 0;
				string = "SELECT * FROM mdl_log WHERE userid = " + course_students.get(l) + " AND cmid = " + recursos_pre.get(m);
				ResultSet log_alunos = this.select(string);
				while (log_alunos.next()) {
					count_log++;
					Associacao_Aluno_Logs ass_al_log = new Associacao_Aluno_Logs();
					ass_al_log.setId_aluno(course_students.get(l));
					ass_al_log.setCount_logs(count_log);
					boolean igual = false;
					int quant_log;
					for (int o = 0; o < alunos_cont_log.size(); o++) {
						if (alunos_cont_log.get(o).getId_aluno().equals(ass_al_log.getId_aluno()) && alunos_cont_log.get(o).getCount_logs() > 0) {
							igual = true;
							quant_log = alunos_cont_log.get(o).getCount_logs();
							quant_log++; // incrementa logs em recursos que s�o pre_req das tarefas
							alunos_cont_log.get(o).setCount_logs(quant_log);
						}
					}
					if (!igual) {
						alunos_cont_log.add(ass_al_log);
						// Adiciona � lista um objeto associacao_aluno_logs, com id do aluno e a quant de logs nos recursos.
					}
				}
			}
		}
		return alunos_cont_log;
	}

	@OPERATION
	ArrayList<String> retorna_pre_requisitos_do_modulo(String idModule) throws SQLException, ClassNotFoundException {
	System.out.println("entrei em retorna pre requisitos do módulo (prévios à atividade calculada) 22");
	String string;
	ArrayList<String> lista_id_module_pre_rec_at_para_mostrar = new ArrayList<String>();
//	this.conexaoBD("retorna_pre_requisitos_do_modulo");
	string = "SELECT pre_req_id FROM mdl_tutor_dependencia WHERE curso_id = "+idCourse+" and rec_ativ_id="+ idModule;
	// pega as atividades que são pre requisito do módulo, para serem mostradas a seguir para os alunos com nota baixa (perfil "básico")
	ResultSet pre_rec_at_ids = this.select(string);
	while (pre_rec_at_ids.next()){	
		System.out.println("entrei no pre req at ids" + pre_rec_at_ids.getString("pre_req_id"));
		if(!pre_rec_at_ids.getString("pre_req_id").equals("0")){
			if (!lista_id_module_pre_rec_at_para_mostrar.contains(pre_rec_at_ids.getString("pre_req_id"))){
				string = "SELECT perfil_id FROM mdl_tutor_rec_at_perfil WHERE curso_id = "+idCourse+" and rec_ativ_id="+ pre_rec_at_ids.getString("pre_req_id");
				ResultSet perfil_id = this.select(string);
				perfil_id.next();
				String perfil_id_basico = perfil_id.getString("perfil_id");
				System.out.println("perfil_id_basico" + perfil_id.getString("perfil_id"));
				if (perfil_id_basico.equals("1") || perfil_id_basico.equals("4")){
				System.out.println("inseri um valor na lista de pre_req para o id_module" + idModule);
				lista_id_module_pre_rec_at_para_mostrar.add(pre_rec_at_ids.getString("pre_req_id"));
				//lista com o id_module e perfil basico ou geral da atividade que deve ser mostrada para os alunos
				}
			}
		}
	}
	//this.fecharConexao();		
	System.out.println("vou sair de retorna pre requisitos do módulo (prévios à atividade calculada)");
	return lista_id_module_pre_rec_at_para_mostrar;
	}

}

