
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class PlacementWebServer {

    static final String DB = "student_placement_system";
    static final String USER = "root";
    static final String PASS = "";

    static final String[] PORTS = {"3306", "3307"};
    static final String MYSQL = "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqld.exe";
    static final String DATA = "C:\\StudentPlacementMySQL\\data";

    // Simple in-memory login sessions for the localhost application
    static final Set<String> SESSIONS = Collections.synchronizedSet(new HashSet<>());
    static final ThreadLocal<HttpExchange> CURRENT_EXCHANGE = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        ensureDatabase();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", PlacementWebServer::handle);
        server.start();

        System.out.println("==============================================");
        System.out.println(" STUDENT PLACEMENT MANAGEMENT SYSTEM");
        System.out.println("==============================================");
        System.out.println("Java + JDBC + MySQL Web Application");
        System.out.println("Open: http://localhost:8080");
        System.out.println("==============================================");
    }

    // ---------- DATABASE ----------

    static Connection connect(String port) throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:" + port + "/" + DB +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                USER, PASS);
    }

    static Connection getConnection() throws SQLException {
        SQLException last = null;
        for (String port : PORTS) {
            try {
                Connection c = connect(port);
                try (Statement s = c.createStatement();
                     ResultSet r = s.executeQuery("SELECT 1 FROM department LIMIT 1")) {
                    return c;
                } catch (SQLException wrongDatabase) {
                    c.close();
                    last = wrongDatabase;
                }
            } catch (SQLException e) {
                last = e;
            }
        }
        throw last;
    }

    static void ensureDatabase() throws Exception {
        try (Connection c = connect("3306")) {
            System.out.println("MySQL connected on port 3306");
            return;
        } catch (SQLException ignored) {}

        try (Connection c = connect("3307")) {
            System.out.println("MySQL connected on port 3307");
            return;
        } catch (SQLException ignored) {}

        File mysql = new File(MYSQL);
        if (!mysql.exists()) {
            throw new Exception("MySQL 8.4 not found at: " + MYSQL);
        }

        System.out.println("Starting project MySQL on port 3307...");
        new ProcessBuilder(
                MYSQL,
                "--datadir=" + DATA,
                "--port=3307",
                "--bind-address=127.0.0.1"
        ).redirectErrorStream(true).start();

        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            try (Connection c = connect("3307")) {
                System.out.println("MySQL connected on port 3307");
                return;
            } catch (SQLException ignored) {}
        }

        throw new Exception(
                "Could not connect to MySQL. Check that the project database exists.");
    }

    // ---------- REQUEST ROUTING ----------

    static void handle(HttpExchange ex) throws IOException {
        CURRENT_EXCHANGE.set(ex);
        String path = ex.getRequestURI().getPath();
        String html;

        try {
            switch (path) {
                case "/":
                    html = dashboard();
                    break;
                case "/students":
                    html = students(ex);
                    break;
                case "/companies":
                    html = companies();
                    break;
                case "/drives":
                    html = drives();
                    break;
                case "/applications":
                    html = applications();
                    break;
                case "/interviews":
                    html = interviews();
                    break;
                case "/results":
                    html = results();
                    break;
                case "/internships":
                    html = internships();
                    break;
                case "/login":
                    html = login();
                    break;
                case "/admin-login":
                    html = loginProcess(ex);
                    break;
                case "/logout":
                    html = logout(ex);
                    break;
                case "/health":
                    html = "<h1>Server is running</h1><p>Java + JDBC + MySQL</p>";
                    break;
                default:
                    html = page("Not Found",
                            "<div class='panel'><h2>Page not found</h2>" +
                            "<a class='btn' href='/'>Back to Dashboard</a></div>");
            }
        } catch (Exception e) {
            html = page("Database Connection Error",
                    "<div class='panel danger'><h2>Database Error</h2>" +
                    "<p>" + esc(e.getMessage()) + "</p>" +
                    "<p>Try restarting the Java server.</p></div>");
        }

        try {
            send(ex, html);
        } finally {
            CURRENT_EXCHANGE.remove();
        }
    }

    // ---------- LAYOUT ----------

    static String page(String title, String body) {
        String template = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>__TITLE__</title>
        <style>
        :root{
            --bg:#070b16;
            --panel:#101729;
            --panel2:#151e34;
            --line:#26314d;
            --text:#eef4ff;
            --muted:#8f9bb5;
            --cyan:#00e5ff;
            --violet:#8b5cf6;
            --green:#20e3a2;
            --pink:#ff4ecd;
            --red:#ff5577;
        }
        *{box-sizing:border-box}
        body{
            margin:0;
            font-family:Arial,Helvetica,sans-serif;
            background:
              radial-gradient(circle at 10% 0%,rgba(0,229,255,.13),transparent 28%),
              radial-gradient(circle at 90% 10%,rgba(139,92,246,.15),transparent 30%),
              var(--bg);
            color:var(--text);
        }
        a{text-decoration:none;color:inherit}
        .top{
            position:sticky;top:0;z-index:10;
            background:rgba(7,11,22,.88);
            backdrop-filter:blur(18px);
            border-bottom:1px solid var(--line);
        }
        .topbar{
            max-width:1450px;margin:auto;padding:16px 24px;
            display:flex;align-items:center;justify-content:space-between;gap:20px;
        }
        .brand{display:flex;align-items:center;gap:13px}
        .logo{
            width:42px;height:42px;border-radius:13px;
            display:grid;place-items:center;
            background:linear-gradient(135deg,var(--cyan),var(--violet));
            color:#06101b;font-weight:900;
            box-shadow:0 0 25px rgba(0,229,255,.35);
        }
        .brand h1{font-size:17px;margin:0}
        .brand small{color:var(--muted)}
        .nav{display:flex;gap:7px;flex-wrap:wrap;justify-content:flex-end}
        .nav a{
            padding:9px 12px;border:1px solid transparent;border-radius:10px;
            color:#b8c4dd;font-size:13px;
        }
        .nav a:hover,.nav a.active{
            color:white;border-color:#33415f;background:#121c32;
            box-shadow:0 0 15px rgba(0,229,255,.08);
        }
        .wrap{max-width:1450px;margin:auto;padding:34px 24px 60px}
        .hero{
            padding:30px;border:1px solid var(--line);border-radius:24px;
            background:linear-gradient(135deg,rgba(16,23,41,.95),rgba(13,18,34,.78));
            box-shadow:0 20px 60px rgba(0,0,0,.25);
            position:relative;overflow:hidden;
        }
        .hero:after{
            content:"";position:absolute;width:250px;height:250px;right:-80px;top:-100px;
            background:var(--cyan);filter:blur(100px);opacity:.12;
        }
        .eyebrow{color:var(--cyan);font-size:12px;text-transform:uppercase;letter-spacing:2px}
        h2{font-size:30px;margin:8px 0}
        .hero p{color:var(--muted);max-width:750px;line-height:1.6}
        .actions{display:flex;gap:10px;flex-wrap:wrap;margin-top:20px}
        .btn{
            display:inline-block;padding:11px 16px;border-radius:10px;
            background:linear-gradient(135deg,var(--cyan),#258cff);
            color:#04101a;font-weight:700;border:0;cursor:pointer;
            box-shadow:0 0 18px rgba(0,229,255,.15);
        }
        .btn.secondary{background:#171f34;color:#dce6fb;border:1px solid var(--line);box-shadow:none}
        .btn.violet{background:linear-gradient(135deg,var(--violet),var(--pink));color:white}
        .stats{
            display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin:20px 0;
        }
        .stat{
            padding:19px;border:1px solid var(--line);border-radius:17px;
            background:rgba(16,23,41,.9);
        }
        .stat .num{font-size:29px;font-weight:800;margin-top:6px}
        .stat .label{color:var(--muted);font-size:12px}
        .grid{
            display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-top:20px;
        }
        .card{
            min-height:190px;padding:20px;border-radius:18px;
            background:linear-gradient(145deg,#111a2d,#0d1425);
            border:1px solid var(--line);
            transition:.2s;
        }
        .card:hover{
            transform:translateY(-4px);border-color:#3b4e77;
            box-shadow:0 15px 35px rgba(0,0,0,.28),0 0 22px rgba(0,229,255,.06);
        }
        .icon{
            width:40px;height:40px;border-radius:12px;display:grid;place-items:center;
            background:#17223a;color:var(--cyan);font-weight:900;
            border:1px solid #293858;
        }
        .card h3{margin:15px 0 7px;font-size:18px}
        .card p{color:var(--muted);line-height:1.5;font-size:13px;min-height:40px}
        .section-title{display:flex;align-items:end;justify-content:space-between;margin-top:30px}
        .section-title h3{margin:0;font-size:20px}
        .section-title span{color:var(--muted);font-size:12px}
        .panel{
            margin-top:20px;padding:20px;border-radius:18px;
            background:rgba(16,23,41,.94);border:1px solid var(--line);
            overflow:auto;
        }
        .panel.danger{border-color:rgba(255,85,119,.4)}
        table{width:100%;border-collapse:collapse;min-width:850px}
        th{
            padding:13px;text-align:left;color:#07111b;
            background:linear-gradient(90deg,var(--cyan),#6ee7ff);
            font-size:12px;
        }
        td{padding:13px;border-bottom:1px solid var(--line);color:#dbe5f8;font-size:13px}
        tr:hover td{background:#131e34}
        .badge{
            display:inline-block;padding:5px 9px;border-radius:999px;
            background:#16263c;color:#7fffea;border:1px solid #28445a;font-size:11px;
        }
        .search{
            display:flex;gap:10px;margin-top:20px;max-width:600px;
        }
        input{
            flex:1;padding:12px 14px;border-radius:10px;
            border:1px solid var(--line);background:#0c1424;color:white;outline:none;
        }
        input:focus{border-color:var(--cyan);box-shadow:0 0 0 3px rgba(0,229,255,.08)}
        .login{
            max-width:430px;margin:60px auto;padding:28px;border-radius:22px;
            background:linear-gradient(145deg,#111a2d,#0b1221);
            border:1px solid var(--line);box-shadow:0 20px 60px rgba(0,0,0,.35);
        }
        .login label{display:block;color:#aebbd2;font-size:13px;margin:15px 0 7px}
        .login input{width:100%}
        .footer{text-align:center;color:#69758e;padding:35px;font-size:12px}
        @media(max-width:1050px){.grid{grid-template-columns:repeat(2,1fr)}.stats{grid-template-columns:repeat(3,1fr)}}
        @media(max-width:650px){.topbar{align-items:flex-start;flex-direction:column}.nav{justify-content:flex-start}.grid,.stats{grid-template-columns:1fr}.wrap{padding:20px 14px}.hero{padding:22px}}
        </style>
        </head>
        <body>
        <div class="top">
          <div class="topbar">
            <a class="brand" href="/">
              <div class="logo">SP</div>
              <div><h1>Student Placement</h1><small>Placement & Internship Hub</small></div>
            </a>
            <div class="nav">
              <a href="/">Home</a>
              <a href="/students">Students</a>
              <a href="/companies">Companies</a>
              <a href="/drives">Drives</a>
              <a href="/applications">Applications</a>
              <a href="/interviews">Interviews</a>
              <a href="/results">Results</a>
              <a href="/internships">Internships</a>
              __ADMIN_NAV__
            </div>
          </div>
        </div>
        <main class="wrap">
        __BODY__
        </main>
        <div class="footer">Student Placement & Internship Management System • Java + JDBC + MySQL</div>
        </body>
        </html>
        """;
        String adminNav;
        if (isLoggedIn()) {
            adminNav = "<a class='active' href='/login'>Admin ✓</a>";
        } else {
            adminNav = "<a href='/login'>Admin</a>";
        }

        return template.replace("__TITLE__", esc(title))
                .replace("__BODY__", body)
                .replace("__ADMIN_NAV__", adminNav);
    }

    // ---------- DASHBOARD ----------

    static String dashboard() throws SQLException {
        boolean admin = isLoggedIn();
        String adminAction = admin
                ? "<a class='btn secondary' href='/login'>Admin Panel ✓</a>"
                : "<a class='btn secondary' href='/login'>Placement Officer Login</a>";
        String adminCard = admin
                ? "<a class='card' href='/login'><div class='icon'>AD</div><h3>Administration</h3><p>Authenticated as Placement Officer. Manage system access.</p><span class='badge'>Authenticated ✓</span></a>"
                : "<a class='card' href='/login'><div class='icon'>AD</div><h3>Administration</h3><p>Placement Officer authentication and system access.</p><span class='badge'>Open module →</span></a>";

        int students = count("student");
        int companies = count("company");
        int drives = count("job_drive");
        int applications = count("application");
        int placements = count("placement_result");

        String body = """
        <section class="hero">
          <div class="eyebrow">Placement Operations • Local Web System</div>
          <h2>One workspace for the complete placement cycle.</h2>
          <p>Monitor students, companies, drives, applications, interviews,
             final selections and internships from one professional dashboard.</p>
          <div class="actions">
            <a class="btn" href="/students">Explore Students</a>
            <a class="btn violet" href="/drives">Explore Job Drives</a>
            __ADMIN_ACTION__
          </div>
        </section>

        <section class="stats">
          <div class="stat"><div class="label">STUDENTS</div><div class="num">__STUDENTS__</div></div>
          <div class="stat"><div class="label">COMPANIES</div><div class="num">__COMPANIES__</div></div>
          <div class="stat"><div class="label">JOB DRIVES</div><div class="num">__DRIVES__</div></div>
          <div class="stat"><div class="label">APPLICATIONS</div><div class="num">__APPLICATIONS__</div></div>
          <div class="stat"><div class="label">PLACED</div><div class="num">__PLACED__</div></div>
        </section>

        <div class="section-title"><h3>Choose a workspace</h3><span>Click any module</span></div>

        <section class="grid">
          <a class="card" href="/students"><div class="icon">ST</div><h3>Students</h3><p>Academic profiles, departments, CGPA and graduation details.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/companies"><div class="icon">CO</div><h3>Companies</h3><p>Participating organizations and their contact information.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/drives"><div class="icon">JD</div><h3>Job Drives</h3><p>Roles, eligibility, packages, deadlines and vacancies.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/applications"><div class="icon">AP</div><h3>Applications</h3><p>Track applications and their current status.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/interviews"><div class="icon">IN</div><h3>Interview Rounds</h3><p>Technical, HR and other interview round outcomes.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/results"><div class="icon">PR</div><h3>Placement Results</h3><p>Final selection, joining date and offered package.</p><span class="badge">Open module →</span></a>
          <a class="card" href="/internships"><div class="icon">IT</div><h3>Internships</h3><p>Internship roles, dates, stipend and completion status.</p><span class="badge">Open module →</span></a>
          __ADMIN_CARD__
        </section>
        """;

        return page("Dashboard",
                body.replace("__STUDENTS__", String.valueOf(students))
                    .replace("__COMPANIES__", String.valueOf(companies))
                    .replace("__DRIVES__", String.valueOf(drives))
                    .replace("__APPLICATIONS__", String.valueOf(applications))
                    .replace("__PLACED__", String.valueOf(placements))
                    .replace("__ADMIN_ACTION__", adminAction)
                    .replace("__ADMIN_CARD__", adminCard));
    }

    // ---------- DATA PAGES ----------

    static String students(HttpExchange ex) throws SQLException {
        String q = param(ex, "q");
        String sql = """
            SELECT s.student_id,s.roll_no,s.name,d.department_name,s.email,s.cgpa,s.graduation_year
            FROM student s JOIN department d ON s.department_id=d.department_id
            WHERE s.name LIKE ? OR s.roll_no LIKE ? OR d.department_name LIKE ?
            ORDER BY s.student_id
            """;
        StringBuilder rows = new StringBuilder();
        try (Connection c=getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            String x="%"+q+"%"; p.setString(1,x); p.setString(2,x); p.setString(3,x);
            try(ResultSet r=p.executeQuery()){
                while(r.next()) rows.append("<tr>")
                    .append(td(r.getInt("student_id"))).append(td(r.getString("roll_no")))
                    .append(td(r.getString("name"))).append(td(r.getString("department_name")))
                    .append(td(r.getString("email"))).append(td(r.getDouble("cgpa")))
                    .append(td(r.getInt("graduation_year"))).append("</tr>");
            }
        }
        String body = title("Students","Student Management","Search students by name, roll number or department.")
            + search("students")
            + table("ID,Roll No,Name,Department,Email,CGPA,Graduation Year",rows.toString());
        return page("Students",body);
    }

    static String companies() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql="SELECT company_id,company_name,industry,location,contact_email,contact_phone FROM company ORDER BY company_id";
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(td(r.getString(5))).append(td(r.getString(6))).append("</tr>");
        }
        return page("Companies",title("Companies","Company Directory","Companies participating in the placement process.")
            + table("ID,Company,Industry,Location,Email,Phone",rows.toString()));
    }

    static String drives() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql=""" 
            SELECT j.drive_id,c.company_name,j.job_role,j.job_type,j.minimum_cgpa,j.package_lpa,
                   j.drive_date,j.application_deadline,j.vacancies
            FROM job_drive j JOIN company c ON j.company_id=c.company_id ORDER BY j.drive_id
            """;
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(td(r.getDouble(5)))
                .append(td(r.getDouble(6)+" LPA")).append(td(r.getDate(7))).append(td(r.getDate(8))).append(td(r.getInt(9))).append("</tr>");
        }
        return page("Job Drives",title("Job Drives","Placement Drive Hub","Eligibility, roles, packages, deadlines and vacancies.")
            + table("ID,Company,Role,Type,Min CGPA,Package,Drive Date,Deadline,Vacancies",rows.toString()));
    }

    static String applications() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql=""" 
            SELECT a.application_id,s.name,c.company_name,j.job_role,a.application_date,a.status
            FROM application a JOIN student s ON a.student_id=s.student_id
            JOIN job_drive j ON a.drive_id=j.drive_id JOIN company c ON j.company_id=c.company_id
            ORDER BY a.application_id
            """;
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(td(r.getDate(5))).append(rawtd("<span class='badge'>"+esc(r.getString(6))+"</span>")).append("</tr>");
        }
        return page("Applications",title("Applications","Application Tracker","See who applied, for which role and the current status.")
            + table("ID,Student,Company,Role,Application Date,Status",rows.toString()));
    }

    static String interviews() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql=""" 
            SELECT ir.round_id,s.name,c.company_name,j.job_role,ir.round_name,ir.round_date,ir.result,ir.remarks
            FROM interview_round ir JOIN application a ON ir.application_id=a.application_id
            JOIN student s ON a.student_id=s.student_id JOIN job_drive j ON a.drive_id=j.drive_id
            JOIN company c ON j.company_id=c.company_id ORDER BY ir.round_id
            """;
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(td(r.getString(5)))
                .append(td(r.getDate(6))).append(rawtd("<span class='badge'>"+esc(r.getString(7))+"</span>")).append(td(r.getString(8))).append("</tr>");
        }
        return page("Interviews",title("Interviews","Interview Control","Track technical, HR and other interview rounds.")
            + table("ID,Student,Company,Role,Round,Date,Result,Remarks",rows.toString()));
    }

    static String results() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql=""" 
            SELECT pr.result_id,s.name,c.company_name,j.job_role,pr.final_status,pr.joining_date,pr.offered_package
            FROM placement_result pr JOIN application a ON pr.application_id=a.application_id
            JOIN student s ON a.student_id=s.student_id JOIN job_drive j ON a.drive_id=j.drive_id
            JOIN company c ON j.company_id=c.company_id ORDER BY pr.result_id
            """;
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(rawtd("<span class='badge'>"+esc(r.getString(5))+"</span>"))
                .append(td(r.getDate(6))).append(td(r.getDouble(7)+" LPA")).append("</tr>");
        }
        return page("Placement Results",title("Placement Results","Final Outcomes","Selected candidates, joining dates and offered packages.")
            + table("ID,Student,Company,Role,Status,Joining Date,Package",rows.toString()));
    }

    static String internships() throws SQLException {
        StringBuilder rows=new StringBuilder();
        String sql="SELECT i.internship_id,s.name,c.company_name,i.internship_role,i.start_date,i.end_date,i.stipend,i.status FROM internship i JOIN student s ON i.student_id=s.student_id JOIN company c ON i.company_id=c.company_id ORDER BY i.internship_id";
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){
            while(r.next()) rows.append("<tr>").append(td(r.getInt(1))).append(td(r.getString(2)))
                .append(td(r.getString(3))).append(td(r.getString(4))).append(td(r.getDate(5))).append(td(r.getDate(6)))
                .append(td("₹"+r.getDouble(7))).append(rawtd("<span class='badge'>"+esc(r.getString(8))+"</span>")).append("</tr>");
        }
        return page("Internships",title("Internships","Internship Hub","Track internship roles, dates, stipend and status.")
            + table("ID,Student,Company,Role,Start Date,End Date,Stipend,Status",rows.toString()));
    }

    // ---------- LOGIN ----------

    static String login() {
        if (isLoggedIn()) {
            String body="""
            <div class="login">
              <div class="eyebrow">AUTHENTICATED</div>
              <h2>Welcome, Placement Officer</h2>
              <p style="color:var(--muted)">You are already logged in to the placement management system.</p>
              <div class="actions">
                <a class="btn" href="/">Open Dashboard</a>
                <a class="btn secondary" href="/logout">Logout</a>
              </div>
            </div>
            """;
            return page("Admin Panel",body);
        }

        String body="""
        <div class="login">
          <div class="eyebrow">SECURE ACCESS</div>
          <h2>Placement Officer</h2>
          <p style="color:var(--muted)">Sign in to the placement management system.</p>
          <form method="GET" action="/admin-login">
            <label>Username</label>
            <input name="username" placeholder="Enter username" required>
            <label>Password</label>
            <input type="password" name="password" placeholder="Enter password" required>
            <div class="actions"><button class="btn" type="submit">Sign In →</button><a class="btn secondary" href="/">Cancel</a></div>
          </form>
        </div>
        """;
        return page("Admin Login",body);
    }

    static String loginProcess(HttpExchange ex) throws SQLException {
        String u=param(ex,"username"), p=param(ex,"password");
        String sql="SELECT name FROM admin WHERE username=? AND password=?";
        try(Connection c=getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,u); ps.setString(2,p);
            try(ResultSet r=ps.executeQuery()){
                if(r.next()){
                    String sessionId = UUID.randomUUID().toString();
                    SESSIONS.add(sessionId);
                    ex.getResponseHeaders().add("Set-Cookie", "PLACEMENT_SESSION=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");

                    return page("Login Successful",
                        "<div class='login'><div class='eyebrow'>AUTHENTICATED</div><h2>Welcome, "+esc(r.getString(1))+"</h2>"+
                        "<p style='color:var(--muted)'>Placement Officer login successful.</p>"+
                        "<div class='actions'><a class='btn' href='/'>Open Dashboard</a><a class='btn secondary' href='/logout'>Logout</a></div></div>");
                }
            }
        }
        return page("Login Failed",
            "<div class='login'><div class='eyebrow' style='color:var(--red)'>ACCESS DENIED</div><h2>Invalid login</h2>"+
            "<p style='color:var(--muted)'>Username or password is incorrect.</p>"+
            "<div class='actions'><a class='btn violet' href='/login'>Try Again</a></div></div>");
    }

    // ---------- LOGIN SESSION ----------

    static boolean isLoggedIn() {
        HttpExchange ex = CURRENT_EXCHANGE.get();
        if (ex == null) return false;

        String cookie = ex.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return false;

        for (String item : cookie.split(";")) {
            String part = item.trim();
            if (part.startsWith("PLACEMENT_SESSION=")) {
                String sessionId = part.substring("PLACEMENT_SESSION=".length());
                return SESSIONS.contains(sessionId);
            }
        }
        return false;
    }

    static String logout(HttpExchange ex) {
        String cookie = ex.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) {
            for (String item : cookie.split(";")) {
                String part = item.trim();
                if (part.startsWith("PLACEMENT_SESSION=")) {
                    SESSIONS.remove(part.substring("PLACEMENT_SESSION=".length()));
                }
            }
        }

        ex.getResponseHeaders().add("Set-Cookie", "PLACEMENT_SESSION=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");

        return page("Logged Out",
                "<div class='login'><div class='eyebrow'>SIGNED OUT</div><h2>Logged out successfully</h2>" +
                "<p style='color:var(--muted)'>Your Placement Officer session has ended.</p>" +
                "<div class='actions'><a class='btn' href='/login'>Sign In Again</a><a class='btn secondary' href='/'>Home</a></div></div>");
    }

    // ---------- HELPERS ----------

    static int count(String table) throws SQLException {
        try(Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM "+table)){
            r.next(); return r.getInt(1);
        }
    }

    static String title(String eyebrow,String h,String p){
        return "<div class='section-title'><div><div class='eyebrow'>"+esc(eyebrow)+"</div><h2>"+esc(h)+"</h2><p style='color:var(--muted)'>"+esc(p)+"</p></div></div>";
    }

    static String search(String path){
        return "<form class='search' method='GET' action='/"+path+"'>"+
               "<input name='q' placeholder='Search students...' value=''>"+
               "<button class='btn' type='submit'>Search</button>"+
               "<a class='btn secondary' href='/"+path+"'>Reset</a></form>";
    }

    static String table(String heads,String rows){
        StringBuilder h=new StringBuilder();
        for(String x:heads.split(",")) h.append("<th>").append(esc(x)).append("</th>");
        if(rows.isEmpty()) rows="<tr><td colspan='20'>No records found.</td></tr>";
        return "<div class='panel'><table><thead><tr>"+h+"</tr></thead><tbody>"+rows+"</tbody></table></div>";
    }

    static String td(Object x){ return "<td>"+esc(String.valueOf(x))+"</td>"; }
    static String rawtd(String x){ return "<td>"+x+"</td>"; }

    static String esc(String x){
        if(x==null) return "";
        return x.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    static String param(HttpExchange ex,String key){
        String q=ex.getRequestURI().getRawQuery();
        if(q==null) return "";
        for(String item:q.split("&")){
            String[] a=item.split("=",2);
            if(a.length==2 && a[0].equals(key))
                return URLDecoder.decode(a[1],StandardCharsets.UTF_8);
        }
        return "";
    }

    static void send(HttpExchange ex,String html) throws IOException{
        byte[] data=html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        ex.sendResponseHeaders(200,data.length);
        try(OutputStream out=ex.getResponseBody()){out.write(data);}
    }
}
