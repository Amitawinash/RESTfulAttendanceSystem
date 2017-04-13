package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import service.UserProvider;
import views.html.*;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String USER_ROLE = "user";

	private final PlayAuthenticate auth;

	private final MyUsernamePasswordAuthProvider provider;

	private final UserProvider userProvider;

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	@Inject
	public Application(final PlayAuthenticate auth, final MyUsernamePasswordAuthProvider provider,
			final UserProvider userProvider) {
		this.auth = auth;
		this.provider = provider;
		this.userProvider = userProvider;
	}

	public Result index() {
		return ok(index.render(this.userProvider));
	}

	@Restrict(@Group(Application.USER_ROLE))
	public Result restricted() {
		final User localUser = this.userProvider.getUser(session());
		return ok(restricted.render(this.userProvider, localUser));
	}

	@Restrict(@Group(Application.USER_ROLE))
	public Result profile() {
		final User localUser = userProvider.getUser(session());
		return ok(profile.render(this.auth, this.userProvider, localUser));
	}

	public Result numberOfClasses() {

		String studentRegistrationId = request().getQueryString("studentRegistrationId");
		String attendanceDate = request().getQueryString("attendanceDate");

		System.out.println("Value from UI :" + "  studentRegistrationId " + studentRegistrationId);

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("attendanceSystem");

		MongoCollection<Document> studentDetails = db.getCollection("StudentDetails");

		System.out.println("Outside try  : ");
		MongoCursor<Document> cursor = null;
		cursor = studentDetails.find(new Document("studentRegistrationId", studentRegistrationId)).iterator();
		List<String> dateOfAttendedClasses = new LinkedList<>();

		try {

			while (cursor.hasNext()) {

				Document article = cursor.next();
				List<String> vals = (List<String>) article.get("dateOfAttendedClasses");
				if (!article.containsValue(studentRegistrationId)) {

					return ok(studentRegistrationIdNotFound.render(studentRegistrationId));

				}

				System.out.println(" Inside else :: " + article.getString("studentRegistrationId"));

				// List<String> vals = (List<String>)
				// article.get("dateOfAttendedClasses");
				System.out.println("val :: " + vals);
				dateOfAttendedClasses.clear();
				dateOfAttendedClasses.addAll(vals);
				System.out.println("Number Of classes ::" + dateOfAttendedClasses.size());

				// String numberOfPresentday =
				// Integer.toString(dateOfAttendedClasses.size());

			}

			
		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}

		String numberOfPresentday = Integer.toString(dateOfAttendedClasses.size());
		return ok(numberOfClasses.render(numberOfPresentday+" and was prasent on these dates:"+dateOfAttendedClasses.toString()));
	}

	public Result attendanceTaken() {

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Inside takeAttendance method :: ");
		System.out.println();
		System.out.println();
		System.out.println();

		String attendanceDate = request().getQueryString("attendanceDate");
		String presentStudantId = request().getQueryString("presentStudantId");
		String course = request().getQueryString("course");
		String semester = request().getQueryString("semester");

		System.out.println("Value from UI :" + "  attendanceDate " + attendanceDate + " semester  :: " + semester
				+ " course  :: " + course);

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("attendanceSystem");

		MongoCollection<Document> studentDetails = db.getCollection("StudentDetails");

		System.out.println("Outside try  : ");

		try {

			MongoCursor<Document> cursor = null;
			cursor = studentDetails.find(new Document("studentRegistrationId", presentStudantId)).iterator();
			System.out.println("Inside try  : " + cursor.toString());

			List<String> dateOfAttendedClasses = new LinkedList<>();

			while (cursor.hasNext()) {

				Document article = cursor.next();
				System.out.println("Val  : " + article.containsKey("dateOfAttendedClasses"));
				if (article.containsValue(presentStudantId)) {

					if (!article.containsKey("dateOfAttendedClasses")) {
						System.out.println("Inside If :: " + article.containsKey("dateOfAttendedClasses"));

						dateOfAttendedClasses.add(attendanceDate);
						System.out.println("Value Of list 1111:: " + dateOfAttendedClasses);

						Bson arg1 = new Document("course", course).append("semester", semester)
								.append("studentRegistrationId", presentStudantId);

						Bson arg0 = new Document("dateOfAttendedClasses", dateOfAttendedClasses);

						Bson updateOpration = new Document("$set", arg0);
						studentDetails.updateOne(arg1, updateOpration);
					} else {

						System.out.println(" Inside else :: " + article.getString("studentRegistrationId"));

						List<String> vals = (List<String>) article.get("dateOfAttendedClasses");
						dateOfAttendedClasses.add(attendanceDate);
						System.out.println("val :: " + vals);
						vals.add(attendanceDate);
						dateOfAttendedClasses.clear();
						dateOfAttendedClasses.addAll(vals);
						System.out.println("Number Of classes ::" + dateOfAttendedClasses.size());
						System.out.println("Value Of list :: " + dateOfAttendedClasses);

						Bson arg1 = new Document("course", course).append("semester", semester)
								.append("studentRegistrationId", presentStudantId);

						Bson arg0 = new Document("dateOfAttendedClasses", dateOfAttendedClasses);

						Bson updateOpration = new Document("$set", arg0);
						studentDetails.updateOne(arg1, updateOpration);
					}

					return ok(attendanceTaken.render("Attendance is given to student registration number : "+presentStudantId));

				}

			}

		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}

		return ok(studentRegistrationIdNotFound.render("Student registration number  : "+presentStudantId+" is wrong."));
	}

	public Result studentAdded() {

		String studentFirstName = request().getQueryString("studentFirstName");
		String studentLastName = request().getQueryString("studentLastName");
		String studentRegistrationId = request().getQueryString("studentRegistrationId");
		String dateOfJoining = request().getQueryString("dateOfJoining");
		String course = request().getQueryString("course");
		String state = request().getQueryString("state");
		String semester = request().getQueryString("semester");
		String studentMobileNumber = request().getQueryString("studentMobileNumber");
		String emailId = request().getQueryString("emailId");

		System.out.println("Value from UI :" + "studentFirstName " + studentFirstName + " studentLastName "
				+ studentLastName + "  studentRegistrationId :: " + studentRegistrationId + "  dateOfJoining :: "
				+ dateOfJoining + "  course :: " + course + " semester  :: " + semester + "  studentMobileNumber :: "
				+ studentMobileNumber + "  emailId :: " + emailId);

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("attendanceSystem");

		MongoCollection<Document> studentDetails = db.getCollection("StudentDetails");

		List<String> list = new ArrayList<String>();
		System.out.println("Outside try  : ");

		try {
			MongoCursor<Document> cursor = null;
			cursor = studentDetails.find(new Document("studentRegistrationId", studentRegistrationId)).iterator();
			System.out.println("Inside try  : " + cursor.toString());

			while (cursor.hasNext()) {

				Document article = cursor.next();
				System.out.println("inside While loop ::: " + article);
				if (article.containsValue(studentRegistrationId)) {

					System.out.println("Id exists");

					return ok(studentNotAdded.render(studentRegistrationId));

				}

				else {

				}

			}

			Document addStudent = new Document("studentFirstName", studentFirstName)
					.append("studentLastName", studentLastName).append("studentRegistrationId", studentRegistrationId)
					.append("dateOfJoining", dateOfJoining).append("course", course).append("state", state)
					.append("semester", semester).append("studentMobileNumber", studentMobileNumber)
					.append("emailId", emailId);

			studentDetails.insertOne(addStudent);

			cursor.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}

		return ok(studentAdded.render(studentRegistrationId+" is not unique."));
	}
@Restrict(@Group(Application.USER_ROLE))
	public Result attendanceHomePage() {

		return ok(attendanceHomePage.render("Your new application is ready."));
	}

	public Result takeAttendance() {

		String course = request().getQueryString("course");
		String semester = request().getQueryString("semester");

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("attendanceSystem");

		MongoCollection<Document> employee = db.getCollection("StudentDetails");

		List<String> list = new ArrayList<String>();
		System.out.println("Outside try  : ");

		try {
			MongoCursor<Document> cursor = null;
			cursor = employee.find(new Document("semester", semester).append("course", course)).iterator();
			System.out.println("Inside try  : " + cursor.toString());

			while (cursor.hasNext()) {

				Document article = cursor.next();

				list.add(article.getString("studentRegistrationId"));
				System.out.println("inside While loop ::: " + article);

			}

		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}

		return ok(takeAttendance.render(list));
	}

	public Result login() {
		return ok(login.render(this.auth, this.userProvider, this.provider.getLoginForm()));
	}

	public Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = this.provider.getLoginForm().bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(login.render(this.auth, this.userProvider, filledForm));
		} else {
			// Everything was filled
			return this.provider.handleLogin(ctx());
		}
	}

	public Result signup() {
		return ok(signup.render(this.auth, this.userProvider, this.provider.getSignupForm()));
	}

	public Result jsRoutes() {
		return ok(play.routing.JavaScriptReverseRouter.create("jsRoutes", routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");

	}

	public Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = this.provider.getSignupForm().bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(this.auth, this.userProvider, filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return this.provider.handleSignup(ctx());
		}
	}
}