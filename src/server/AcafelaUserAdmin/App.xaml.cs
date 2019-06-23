using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;

namespace AcafelaUserAdmin
{
    public partial class App : Application
    {
        public UserAdminModel UserAdminModel
        {
            get;
            private set;
        }

        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            UserAdminModel = new UserAdminModel();
        }
    }
}
