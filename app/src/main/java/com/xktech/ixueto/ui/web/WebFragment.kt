package com.xktech.ixueto.ui.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentWebBinding
import com.xktech.ixueto.model.Cookie

class WebFragment : Fragment() {
    private var binding: FragmentWebBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var webView: com.xktech.ixueto.components.WebView
    private val args: WebFragmentArgs by navArgs()
    private lateinit var title: String
    private lateinit var url: String
    private lateinit var cookies: Array<Cookie>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initView()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        toolBar.setNavigationIcon(R.drawable.ic_toolbar_close)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            WebFragment()
    }

    private fun initView() {
        binding = FragmentWebBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        webView = binding!!.webView
        navController = findNavController()
        title = args.title.toString()
        url = args.url
        if (args.cookies != null) {
            cookies = args.cookies!!
            for (cookie in cookies) {
                webView.cookieManager.setCookie(cookie.url, cookie.value)
            }
        }
        webView.loadUrl(url)
        toolBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.refresh->{
                    webView.reload()
                }
                R.id.jump_browser->{
                    jumpUriToBrowser(requireContext(), url)
                }
            }
            true
        }
//        webView.setOnLoadStatueListener(object : MyWebView.OnWebLoadStatusListener {
//            override fun error() {
//                empty_view.setEmptyImage(R.drawable.load_fail)
//                empty_view.setEmptyText(R.string.net_error_again)
//            }
//
//            override fun success() {
//                empty_view.setGone()
//            }
//
//            override fun onTitle(title: String) {
//                tv_title.text = title
//            }
//
//        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destory()
        binding = null
    }

    // 根据网址跳转第三方浏览器
    private fun jumpUriToBrowser(context: Context, url: String) {
        var url = url
        if (url.startsWith("www.")) url = "http://$url"
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(context, "网址错误", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent()
        // 设置意图动作为打开浏览器
        intent.action = Intent.ACTION_VIEW
        // 声明一个Uri
        val uri: Uri = Uri.parse(url)
        intent.data = uri
        context.startActivity(intent)
    }

}