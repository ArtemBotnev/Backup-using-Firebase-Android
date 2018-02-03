package ru.artembotnev.basesaver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_layout.*
import kotlinx.android.synthetic.main.app_bar.*

import ru.artembotnev.basesaver.fbHelper.FiredataClothes
import ru.artembotnev.basesaver.imageTransform.CircleTransformation


/**
 * Created by Artem Botnev on 21.12.2017.
 */

abstract class ParentActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FiredataClothes.OnReceiveDataListener {

    companion object {
        private const val TAG = "ParentActivity"
        private const val SIGN_IN = 102
        private const val DATA = "data"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var imageView: ImageView //for user's icon
    private lateinit var nameView: TextView //for user's nickname
    private lateinit var emailView: TextView //for user's email

    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)
        setSupportActionBar(toolbar)

        //add fragment into activity
        var fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = createFragment()
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }

        buildDrawer()

        val headView = navView.getHeaderView(0)
        imageView = headView.findViewById(R.id.image_view)
        nameView = headView.findViewById(R.id.user_name)
        emailView = headView.findViewById(R.id.user_email)

        // configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.my_client_ID)) // your server's web client ID
//                .requestIdToken(getString(R.string.default_web_client_id)) //default version
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // initialize_auth
        auth = FirebaseAuth.getInstance()

        // selections title of auth item
        val signInOutItem = navView.menu.findItem(R.id.sign_in_out)
        signInOutItem.title = if (auth.currentUser == null) {
            getString(R.string.sign_in)
        } else {
            getString(R.string.sign_out)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.sign_in_out -> signInOut(item)
            R.id.send_data -> sendData()
            R.id.receive_data -> receiveData()
        }

        navDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (navDrawer.isDrawerOpen(GravityCompat.START)) {
            navDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // method of FiredataClothes.OnReceiveDataListener
    override fun receiveFiredata(items: List<ClothesItem>?) {
        if (items != null) {
            Wardrobe.getInstance(this).addList(items)

            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is ListFragment) {
                fragment.updateUI()
            }

        } else {
            pushToast(R.string.data_is_not_retrieved)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN) {
            // data is  googleSignInClient.signInIntent
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
                Log.w(TAG, "Google sign in successful")
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                pushToast(R.string.sing_in_failed)
            }
        }
    }

    //create action bar
    private fun buildDrawer() {
        val toggle = ActionBarDrawerToggle(this, navDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        navDrawer.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    private fun updateUI(user: FirebaseUser?) {
        loadUserImage(imageView, user?.photoUrl)

        val logIn = user?.displayName ?: getString(R.string.menu)
        val email = user?.email ?: ""

        nameView.text = logIn
        emailView.text = email
    }

    //getting user's icon
    private fun loadUserImage(view: ImageView, photoUrl: Uri?) {
        Picasso.with(this)
                .load(photoUrl)
                .transform(CircleTransformation())
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(view)
    }

    private fun signInOut(item: MenuItem) {
        if (auth.currentUser == null) {
            item.title = getString(R.string.sign_out)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, SIGN_IN)
        } else {
            item.title = getString(R.string.sign_in)
            auth.signOut()
            googleSignInClient.signOut()
                    .addOnCompleteListener { updateUI(null) }
        }
    }

    // send data to Firebase database
    private fun sendData() {
        val user = getRegisteredUserWithToast() ?: return

        val clothes = Wardrobe.getInstance(this).getClothes() // get data from SQLite
        FiredataClothes(DATA, user.uid).send(clothes) // send data to Firebase database
        pushToast(R.string.data_is_sent)
    }

    // retrieve data from Firebase database
    private fun receiveData() {
        val user = getRegisteredUserWithToast() ?: return
        FiredataClothes(DATA, user.uid).receive(this)
    }

    private fun getRegisteredUserWithToast(): FirebaseUser? {
        val user = auth.currentUser
        if (user == null) {
            pushToast(R.string.need_register)
        }

        return user
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct!!.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) {
                    if (it.isComplete) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        pushToast(R.string.sing_in_successful)
                        updateUI(auth.currentUser)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                        pushToast(R.string.sing_in_failed)
                        updateUI(null)
                    }
                }
    }

    private fun pushToast(stringId: Int) {
        Toast.makeText(this, getString(stringId), Toast.LENGTH_SHORT).show()
    }
}