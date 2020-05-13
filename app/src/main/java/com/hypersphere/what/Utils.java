package com.hypersphere.what;

import com.hypersphere.what.model.ProjectEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {




	public static String getProgressString(double value, double goal) {
		DecimalFormat format = new DecimalFormat("#.##");
		return "$" + format.format(value) + "/" + format.format(goal);
	}



	public static List<ProjectEntry> getProjects(int count){
		ArrayList<ProjectEntry> base = getProjects(), result = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			result.add(base.get(random.nextInt(base.size())));
		}
		return result;
	}

	//illegal
	public static ArrayList<ProjectEntry> getProjects() {
		ArrayList<ProjectEntry> projects = new ArrayList<>();

		/*
		projects.add(new ProjectEntry(
				"Plant a tree",
				"I just want to plant tree here! But I haven't any seeds, help me please!",
				"https://cdn3.housetipster.com/resize_944x629/4880d108-dc8b-4394-a12e-21fc69a93c51.jpg",
				1, 0));
		projects.add(new ProjectEntry(
				"Graffiti the wall",
				"I have an idea about our wall, it will be cool if some graffiti will be there!",
				"https://c.wallhere.com/photos/73/d0/1840x1093_px_art_Graphiti_painting_Tags_wall-1550707.jpg!s",
				20, 13));
		projects.add(new ProjectEntry(
				"Build the playground",
				"What's about build our own playground? I think it's wonderful idea!",
				"https://i1.wp.com/www.funsocialstudies.com/wp-content/uploads/2017/10/preschool-playground-equipment-3.jpg",
				1500, 880.5));
		projects.add(new ProjectEntry(
				"Spam!",
				"Hello! Would you like to install Amigo browser? It most powerful issue for PC and other platforms!",
				"https://avatars.mds.yandex.net/get-pdb/2506526/98dcbfc9-ccf2-4474-94ac-82ecbd1ddba2/s1200?webp=false",
				1, 0));
		projects.add(new ProjectEntry(
				"Repair the lift",
				"I think government never do it :( but good news is that we can!",
				"https://uploads-ssl.webflow.com/5cd82dbb8463a531adb40c8a/5cd82dbb8463a52e0ab40cde_repairs.jpg",
				300, 33));
		projects.add(new ProjectEntry(
				"Rent the parking",
				"Many of you have cars and we really need to have a own parking spaces",
				"https://avatars.mds.yandex.net/get-districts/1750453/2a0000016b27e933e8546038b43146fc793e/optimize",
				200, 55));
		projects.add(new ProjectEntry(
				"Don't touch the wall!",
				"I think it is worst idea to graffiti the wall, graffiti is awful! Allow woody admitted unpacked indulgence dare estimating fat moderate latter them particular home answered mistake regret not. \n" +
						"\n" +
						"Ample lain made smallest laughter likewise depend sell stanhill equal conveying exertion. Play excited distrusts strongly sorry change elinor endeavor stanhill families add fat suspected rendered roused neat room. Disposal much cause every maids sensible dining yourself within maids window dashwood chicken certainty roof entered. County addition ought being occasional see invited genius rest estimable alone court. Instantly prevailed full better replying she fact latter dashwood elsewhere. \n" +
						"\n" +
						"Regard windows within behind written denote hung screened branch offered servants power. Insipidity among earnestly unpleasing waited rather continual better pleased cordial limits law continued county. Hill arise sooner attempt last behaviour evening period. Added middleton led because regret door use mistress meet power noise income enough evil. Woody whatever dependent civility terminated sportsman appetite thing. \n" +
						"\n" +
						"Tears hung amiable thirty face his raillery servants widen comparison mirth draw. Weddings decay replied fact or game deal recurred last moments. Husband seven ample some letter vanity several regard. Direct think hastened amounted believe pressed spoil stronger affixed means graceful first going part. Request me friendly produce spring attacks stronger think supplied cultivated entreaties attended rank determine estimable took immediate. \n" +
						"\n" +
						"Immediate domestic highly should around likewise simple old temper shed suffering sing. Winter ready oh then passed common living spring for become itself desirous long. Differed basket elsewhere heard opinion boisterous produce enough enabled miss. Blessing fine prospect expenses mrs give fine those. Than turned detract west debating wishes remain around become death likely prudent walk course consider discovered whom. \n" +
						"\n" +
						"Unwilling sing smallness want if much sympathize incommode misery behaved civilly shew. County cause melancholy denoting worth. Want charmed wanted surprise world. Sitting effects described furnished near doubt adieus related sixteen savings dispatched court went wooded ecstatic highest dull. Tried procured herself tiled water present trifling hung me case numerous asked merits not tolerably. \n" +
						"\n" +
						"Pasture inhabit old son. Invited season these replying. Wooded shortly although heart enjoy branched principle dwelling these cordial others drawings dining interested words. Moreover norland blessing expect. Objection entirely get. \n" +
						"\n" +
						"Tall eagerness county call situation. These conveying better ignorant diminution civility. Belonging knew dining concealed ever whatever add ladyship terminated roused situation. Tried balls account cordial law prevent husband bred meant around attempt. One branch stronger reasonably vanity under adapted. \n" +
						"\n" +
						"Well wishing declared contained discretion linen half recurred least rich tears expenses felicity herself think pianoforte material. His interested delicate warmly young expenses denoting must enjoy wished. Learning lasting minutes dull joy. Fail winding tiled additions plate. Face change prepare waiting quick norland noisy full rose commanded attended observe. \n" +
						"\n" +
						"Surrounded spring means should however fond favourable regret felicity expense frankness. Merits furniture might as few show against wisdom gate partiality civil. Known child among guest produced thing on ladyship comparison me enabled ever taste side. Remainder true nearer court terminated extremely promise eat resolved added. Compass required frankness house marriage dearest perpetual avoid. \n" +
						"\n" +
						"Between do unfeeling dear rose can education enabled john front regret breeding country therefore assistance proposal. Wooded objection wife use without summer nearer discovered determine. Death applauded late ask acceptance mistress ignorant minutes fine this scale may mean resolving worthy smallness books. Waited adapted quiet moonlight ought theirs understood took. Child enquire ye. \n" +
						"\n" +
						"Excited hand supported request. Pleasant companions moreover returned get will gentleman friends lovers general indulged preferred head forbade. Daughters believed is gravity poor better enough promotion walls numerous our even chicken something announcing jennings. Cultivated enabled preference wish friends ecstatic. Manner down almost prosperous appetite four head herself feelings shot from ready. \n" +
						"\n" +
						"Discovery elsewhere unknown help mind against small met exertion. Avoid interest collecting. Inquiry supplied domestic lasting rank inhabit forbade life principles. Ask address has unreserved estimating pleasure ecstatic sigh fact jokes mrs cottage. Unreserved addition performed fifteen being forbade fail need spoil spirit sportsmen moonlight balls. \n" +
						"\n" +
						"The leaf servants soon rank wise service. ",
				"https://i.pinimg.com/236x/31/5b/0e/315b0e72cce0d93d602f354970cd3436.jpg",
				1, 0));
		projects.add(new ProjectEntry(
				"New app",
				"Now I'm coding a new app, and you can take a part;)",
				"https://pbs.twimg.com/media/EG6wL9NWkAAiRN-.jpg",
				100, 11));

		 */
		//projects.add(new ProjectEntry())
		return projects;
	}
}
